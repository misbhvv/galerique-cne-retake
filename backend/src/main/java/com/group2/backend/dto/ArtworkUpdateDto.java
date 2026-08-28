package com.group2.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtworkUpdateDto {
    private String title;
    private String description;
    private BigDecimal price;
    private Integer year;
    private List<String> tags;
}
