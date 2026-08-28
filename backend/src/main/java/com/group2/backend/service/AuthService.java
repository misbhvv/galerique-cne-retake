package com.group2.backend.service;

import com.group2.backend.exception.auth.AuthException;
import com.group2.backend.exception.model.ModelException;
import com.group2.backend.model.Account;
import com.group2.backend.model.Token;
import com.group2.backend.repository.AccountRepository;
import com.group2.backend.repository.TokenRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String COOKIE_NAME = "cloud_native_engeneering_group2_session";
    private static final int DEFAULT_TTL_SECONDS = 24 * 60 * 60; // 24h
    private static final int REMEMBER_TTL_SECONDS = 30 * 24 * 60 * 60; // 30d

    private final AccountRepository accountRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.cookie.domain:}")
    private String cookieDomain;

    @Value("${app.cookie.same-site:}")
    private String cookieSameSite;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Authenticate by identifier and password
     * Returns generated token uid when successful
     */
    @Transactional
    public String authenticate(String email, String password, int ttlSeconds) {
        Account account = accountRepository.findByEmail(email)
            .orElseThrow(() -> new AuthException("Invalid credentials"));

        boolean verified = false;

        if (account.getPassword() != null && account.getPassword().startsWith("$2")) {
            verified = passwordEncoder.matches(password, account.getPassword());
        } else {
            if (password.equals(account.getPassword())) {
                verified = true;
                account.setPassword(passwordEncoder.encode(password));
                accountRepository.save(account);
            }
        }

        if (!verified) {
            throw new AuthException("Invalid credentials");
        }

        Token token = Token.builder()
            .uid(UUID.randomUUID().toString())
            .expiresAt(Instant.now().plusSeconds(ttlSeconds))
            .account(account)
            .build();

        tokenRepository.save(token);

        return token.getUid();
    }

    /**
     * Public login helper which validates input and delegates to authenticate
     */
    @Transactional
    public String login(String email, String password, boolean remember) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new ModelException("email and password required");
        }

        int ttl = remember ? REMEMBER_TTL_SECONDS : DEFAULT_TTL_SECONDS;
        return authenticate(email, password, ttl);
    }

    /**
     * Login and set auth cookie on the current HTTP response.
     * Throws ModelException or AuthException on failure.
     * Sends login notification email on success.
     */
    @Transactional
    public void loginAndSetCookie(String email, String password, boolean remember) {
        int ttl = remember ? REMEMBER_TTL_SECONDS : DEFAULT_TTL_SECONDS;
        String uid = login(email, password, remember);
        HttpServletResponse response = getCurrentResponse();
        setCookie(response, COOKIE_NAME, uid, ttl);
    }

    /**
     * Validate token UID and return associated Account
     */
    @Transactional(readOnly = true)
    public Account getAccountFromToken(String uid) {
        if (uid == null || uid.isBlank()) {
            throw new AuthException("Missing token");
        }

        Token token = tokenRepository.findByUid(uid)
            .orElseThrow(() -> new AuthException("Invalid token"));

        if (token.getExpiresAt() != null && token.getExpiresAt().isBefore(Instant.now())) {
            throw new AuthException("Token expired");
        }

        if (token.getAccount() == null) {
            throw new AuthException("Account not found for token");
        }

        String accountId = token.getAccount().getId();
        return accountRepository.findById(accountId)
            .orElseThrow(() -> new AuthException("Account not found"));
    }

    /**
     * Read token from current HTTP request cookies and return associated account
     */
    public Account getAccountFromRequest() {
        HttpServletRequest request = getCurrentRequest();

        String tokenUid = getTokenFromCookies(request);
        if (tokenUid == null || tokenUid.isBlank()) {
            throw new AuthException("Unauthorized - Missing token");
        }

        return getAccountFromToken(tokenUid);
    }

    /**
     * Logout current session. If hard=true remove all tokens for the account.
     */
    @Transactional
    public void logout(boolean hard) {
        HttpServletRequest request = getCurrentRequest();
        HttpServletResponse response = getCurrentResponse();

        String tokenUid = getTokenFromCookies(request);

        if (hard) {
            if (tokenUid == null || tokenUid.isBlank()) {
                throw new ModelException("Hard logout requires an active token cookie");
            }

            Account account = getAccountFromToken(tokenUid);
            tokenRepository.deleteByAccountId(account.getId());

            deleteCookie(response, COOKIE_NAME);
            return;
        }

        if (tokenUid != null && !tokenUid.isBlank()) {
            tokenRepository.deleteByUid(tokenUid);
            deleteCookie(response, COOKIE_NAME);
        }
    }

    /**
     * 
     * Change password for current authenticated user
     * 
     * @param newPassword The new password to set
     * @return true if password changed successfully
     **/
    @Transactional
    public boolean changePassword(String newPassword) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof String accountId)) {
            throw new AuthException("Unsupported principal type");
        }

        Account caller = accountRepository.findById(accountId)
                .orElseThrow(() -> new AuthException("Account not found"));

        caller.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(caller);
        return true;
    }

    // Helper methods to access current request/response and cookies
    
    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new AuthException("No HTTP context available");
        }
        return attributes.getRequest();
    }

    private HttpServletResponse getCurrentResponse() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new AuthException("No HTTP context available");
        }
        return attributes.getResponse();
    }

    private String getTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        return Arrays.stream(cookies)
            .filter(c -> COOKIE_NAME.equals(c.getName()))
            .map(Cookie::getValue)
            .findFirst()
            .orElse(null);
    }

    private void deleteCookie(HttpServletResponse response, String cookieName) {
        setCookie(response, cookieName, "", 0);
    }

    private void setCookie(HttpServletResponse response, String name, String value, int maxAgeSeconds) {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("=").append(value == null ? "" : value);
        sb.append("; Path=/");
        sb.append("; HttpOnly");
        sb.append("; Secure");
        sb.append("; Max-Age=").append(maxAgeSeconds);
        if (cookieDomain != null && !cookieDomain.isBlank()) {
            sb.append("; Domain=").append(cookieDomain);
        }
        if (cookieSameSite != null && !cookieSameSite.isBlank()) {
            sb.append("; SameSite=").append(cookieSameSite);
        }
        response.addHeader("Set-Cookie", sb.toString());
    }

}
