package com.group2.backend.security;

import com.group2.backend.model.Token;
import com.group2.backend.model.Account;
import com.group2.backend.repository.TokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthTokenFilter extends OncePerRequestFilter {

    private final TokenRepository tokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws java.io.IOException, jakarta.servlet.ServletException {

        try {
            Optional<Cookie> tokenCookie = Optional.ofNullable(request.getCookies())
                    .flatMap(cookies -> 
                        java.util.Arrays.stream(cookies)
                            .filter(c -> "cloud_native_engeneering_group2_session".equals(c.getName()))
                            .findFirst()
                    );

            if (tokenCookie.isPresent()) {
                String tokenUid = tokenCookie.get().getValue();
                Optional<Token> tokenOpt = tokenRepository.findByUid(tokenUid);

                if (tokenOpt.isPresent() && !tokenOpt.get().isExpired()) {
                    Token token = tokenOpt.get();
                    Account account = token.getAccount();
                    
                    if (account == null) {
                        log.warn("Token has no associated account: {}", tokenUid);
                    } else {
                        // Use account id as principal to avoid lazy proxy outside session
                        UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(account.getId(), null, java.util.Collections.emptyList());

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } else {
                    log.warn("Invalid or expired token: {}", tokenUid);
                }
            } else {
            }

        } catch (Exception ex) {
            log.error("Failed to authenticate user by token", ex);
        }

        filterChain.doFilter(request, response);
    }
}
