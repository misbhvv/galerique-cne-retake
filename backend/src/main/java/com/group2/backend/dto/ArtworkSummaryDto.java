package com.group2.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtworkSummaryDto {
    private String id;
    private String title;
    private String imageUrl;
    private String thumbnailUrl;
    private BigDecimal price;
    private Integer year;
    private int views;
    private boolean sold;
    private Instant createdAt;
    private AccountSummaryDto creator;
    private List<String> tags;
    private Integer searchScore;
}
