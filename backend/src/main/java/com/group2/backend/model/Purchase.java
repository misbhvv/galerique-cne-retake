package com.group2.backend.model;

import com.group2.backend.dto.AccountSummaryDto;
import com.group2.backend.dto.ArtworkSummaryDto;
import com.group2.backend.dto.PurchaseDto;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;

@Document(collection = "purchases")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Purchase {
    @Id
    private String id;


    @NotNull
    private BigDecimal purchasePrice;

    @CreatedDate
    private Instant purchaseDate;

    @DBRef(lazy = true)
    @NotNull
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Account buyer;

    @DBRef(lazy = true)
    @NotNull
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Artwork artwork;

    public PurchaseDto toDto() {
        AccountSummaryDto buyerDto = buyer != null ? buyer.toSummaryDto() : null;
        ArtworkSummaryDto artworkDto = artwork != null ? artwork.toSummaryDto() : null;

        return PurchaseDto.builder()
            .id(this.id)
            .purchasePrice(this.purchasePrice)
            .purchaseDate(this.purchaseDate)
            .buyer(buyerDto)
            .artwork(artworkDto)
            .build();
    }

    // Validation that is not handled by JPA anymore - needs to be implemented elsewhere
    /*private void validateState() {
        if (purchasePrice == null) throw new IllegalStateException("Purchase price is required");
        if (buyer == null) throw new IllegalStateException("Purchase requires a buyer");
        if (artwork == null) throw new IllegalStateException("Purchase requires an artwork");
    }*/
}
