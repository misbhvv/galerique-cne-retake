package com.group2.backend.repository;

import com.group2.backend.model.Account;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface AccountRepository extends MongoRepository<Account, String> {
    
    Optional<Account> findByEmail(String email);

    Optional<Account> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<Account> findWithTokensById(String id);


    
}
