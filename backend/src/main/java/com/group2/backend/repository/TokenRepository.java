package com.group2.backend.repository;

import com.group2.backend.model.Token;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends MongoRepository<Token, String> {
    

    Optional<Token> findByUid(String uid);

    Optional<Token> findWithAccountByUid(String uid);

    List<Token> findByAccountId(String accountId);

    void deleteByUid(String uid);

    void deleteByAccountId(String accountId);

    List<Token> findByExpiresAtBefore(Instant expiresAt);

    int deleteByExpiresAtBefore(Instant expiresAt);

    boolean existsByUid(String uid);
}
