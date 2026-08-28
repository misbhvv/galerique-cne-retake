package com.group2.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseDto {
    private String id;
    private BigDecimal purchasePrice;
    private Instant purchaseDate;
    private ArtworkSummaryDto artwork;
    private AccountSummaryDto buyer;
}
