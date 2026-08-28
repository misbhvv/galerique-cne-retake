package com.group2.backend.service;

import com.group2.backend.dto.AccountCreateDto;
import com.group2.backend.dto.AccountDto;
import com.group2.backend.dto.AccountSummaryDto;
import com.group2.backend.dto.AccountUpdateDto;
import com.group2.backend.exception.service.ServiceException;
import com.group2.backend.model.Account;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.group2.backend.repository.AccountRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;


@Service
@Transactional
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;


    @Cacheable("allAccounts")
    public List<AccountSummaryDto> getAccounts() {
        return accountRepository.findAll().stream()
                .map(Account::toSummaryDto)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "accounts", key = "#id")
    public AccountDto getAccount(String id) {
        Account account = accountRepository.findById(id)
				.orElseThrow(() -> new ServiceException("Account not found", HttpStatus.NOT_FOUND));
        return account.toDto();
    }


    public AccountDto getCurrentAccount() {
        Account currentAccount = authService.getAccountFromRequest();
        return currentAccount.toDto();
    }

    @CacheEvict(value = "allAccounts", allEntries = true)
    public AccountDto createAccount(AccountCreateDto body) {
        if (accountRepository.existsByUsername(body.getUsername())) {
            throw new ServiceException("Account with username already exists", HttpStatus.BAD_REQUEST);
        }

        if (accountRepository.existsByEmail(body.getEmail())) {
            throw new ServiceException("Account with email already exists", HttpStatus.BAD_REQUEST);
        }




        Account newAccount = Account.builder()
            .username(body.getUsername())
            .email(body.getEmail())
            .password(passwordEncoder.encode(body.getPassword()))
            .build();

        Account saved = accountRepository.save(newAccount);
        return saved.toDto();
    }

    /**
     * Update the currently authenticated user's profile (email, username).
     */
    @Caching(evict = {
        @CacheEvict(value = "accounts", allEntries = true),
        @CacheEvict(value = "allAccounts", allEntries = true)
    })
    public AccountDto updateCurrentAccount(AccountUpdateDto body) {
        Account currentAccount = authService.getAccountFromRequest();

        if (body.getUsername() != null && !body.getUsername().isBlank()) {
            String trimmed = body.getUsername().trim();
            if (!trimmed.equals(currentAccount.getUsername())) {
                if (accountRepository.existsByUsername(trimmed)) {
                    throw new ServiceException("Account with username already exists", HttpStatus.BAD_REQUEST);
                }
                currentAccount.setUsername(trimmed);
            }
        }

        if (body.getEmail() != null && !body.getEmail().isBlank()) {
            String trimmed = body.getEmail().trim().toLowerCase();
            if (!trimmed.equals(currentAccount.getEmail())) {
                if (accountRepository.existsByEmail(trimmed)) {
                    throw new ServiceException("Account with email already exists", HttpStatus.BAD_REQUEST);
                }
                currentAccount.setEmail(trimmed);
            }
        }

        Account saved = accountRepository.save(currentAccount);
        return saved.toDto();
    }

}
