package com.group2.backend.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "tags")
@CompoundIndexes({
        @CompoundIndex(name = "idx_tag_normalized_name", def = "{'normalizedName': 1}", unique = true),
        @CompoundIndex(name = "idx_tag_usage_name", def = "{'usageCount': -1, 'name': 1}")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {

    @Id
    private String id;

    @Indexed
    private String name;

    private String normalizedName;

    private String description;

    @Indexed
    @Builder.Default
    private int usageCount = 0;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @DBRef
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<TagAlias> aliases = new ArrayList<>();
}
