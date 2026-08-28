package com.group2.backend.model;

import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "artwork_likes")
@CompoundIndex(name = "account_artwork_idx", def = "{'account': 1, 'artwork': 1}", unique = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtworkLike {
    @Id
    private String id;

    @CreatedDate
    private Instant createdAt;

    @DBRef(lazy = true)
    @NotNull
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Account account;

    @DBRef(lazy = true)
    @NotNull
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Artwork artwork;

    // Validation that is not handled by JPA anymore - needs to be implemented elsewhere
    /*private void validateState() {
        if (account == null) throw new IllegalStateException("ArtworkLike requires an account");
        if (artwork == null) throw new IllegalStateException("ArtworkLike requires an artwork");
    }*/
}
