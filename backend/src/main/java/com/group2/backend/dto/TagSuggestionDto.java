package com.group2.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagSuggestionDto {
    private String id;
    private String name;
    private String description;
    private Integer usageCount;
}
