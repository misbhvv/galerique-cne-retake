package com.group2.backend.model;


import jakarta.validation.constraints.*;
import lombok.*;


import java.time.Instant;
import java.util.List;

import com.group2.backend.dto.AccountDto;
import com.group2.backend.dto.AccountSummaryDto;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "accounts")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {
    @Id
    private String id;

    @Indexed(unique = true)
    @NotBlank
    @Size(max = 50)
    private String username;

    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    @NotBlank
    private String password;

    @DBRef(lazy = true)
    @Singular
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Token> tokens;





    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @DBRef(lazy = true)
    @Singular
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Artwork> artworks;

    @DBRef(lazy = true)
    @Singular
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Purchase> purchases;

    @DBRef(lazy = true)
    @Singular
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ArtworkLike> likes;

    /**
     * Convert to AccountDto
     */
    public AccountDto toDto() {
        return AccountDto.builder()
            .id(this.id)
            .username(this.username)
            .email(this.email)
            .build();
    }

    /**
	 * Convert to AccountSummaryDto
	 */
	public AccountSummaryDto toSummaryDto() {
		return AccountSummaryDto.builder()
			.id(this.id)
			.username(this.username)
			.build();
	}

}
