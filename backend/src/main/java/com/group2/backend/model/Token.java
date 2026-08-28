package com.group2.backend.model;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;

import com.group2.backend.exception.model.ModelException;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Token {
    @Id
    private String id;

    @NotBlank
    @Builder.Default
    private String uid = "";

    @CreatedDate
    private Instant createdAt;

    @Indexed(expireAfter = "0s")
    private Instant expiresAt;

    @DBRef(lazy = true)
    @NotNull
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Account account;

    /**
     * Check if this token is expired
     * @return true if expired, false otherwise
     */
    public boolean isExpired() {
        return expiresAt != null && expiresAt.isBefore(Instant.now());
    }

    // Validation that is not handled by JPA anymore - needs to be implemented elsewhere
    /*private void validateState() {
        if (uid == null || uid.trim().isEmpty()) {
            throw new ModelException("Token UID is required");
        }
        uid = uid.trim();
        if (account == null) {
            throw new ModelException("Token requires an account");
        }
        if (expiresAt != null && expiresAt.isBefore(createdAt != null ? createdAt : Instant.now())) {
            throw new ModelException("Token expiration must be after creation");
        }
    }*/
}
