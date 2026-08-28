package com.group2.backend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "tag_aliases")
@CompoundIndexes({
        @CompoundIndex(name = "idx_tag_alias_normalized", def = "{'normalizedAlias': 1}", unique = true),
        @CompoundIndex(name = "idx_tag_alias_tag", def = "{'tagId': 1}")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagAlias {

    @Id
    private String id;

    private String alias;

    private String normalizedAlias;

    @DBRef(lazy = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Tag tag;
}
