package com.group2.backend.model;

import com.group2.backend.dto.AccountSummaryDto;
import com.group2.backend.dto.ArtworkDto;
import com.group2.backend.dto.ArtworkImageDto;
import com.group2.backend.dto.ArtworkSummaryDto;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Document(collection = "artworks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Artwork {
    @Id
    private String id;

    @NotBlank
    @Size(max = 255)
    private String title;

    private String description;

    @NotNull
    private BigDecimal price;

    private Integer year;

    @Indexed
    @Builder.Default
    private int views = 0;

    @Builder.Default
    private boolean sold = false;

    @Indexed
    @CreatedDate
    private Instant createdAt;

    @DBRef(lazy = true)
    @NotNull
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Account creator;

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

    @DBRef(lazy = true)
    @Singular
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ArtworkImage> images;

    @DBRef
    @Builder.Default
    private List<Tag> tags = new ArrayList<>();

    public ArtworkDto toDto() {
        AccountSummaryDto creatorDto = creator != null ? creator.toSummaryDto() : null;
        List<ArtworkImageDto> imageDtos = images == null
            ? List.of()
            : images.stream()
                .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                .map(image -> image.toDto(null, null))
                .toList();

        ArtworkImageDto mainImage = imageDtos.stream()
            .filter(ArtworkImageDto::isMainImage)
            .findFirst()
            .orElseGet(() -> imageDtos.isEmpty() ? null : imageDtos.get(0));

        return ArtworkDto.builder()
            .id(this.id)
            .title(this.title)
            .description(this.description)
            .imageUrl(mainImage != null ? mainImage.getUrl() : null)
            .thumbnailUrl(mainImage != null ? mainImage.getThumbnailUrl() : null)
            .price(this.price)
            .year(this.year)
            .views(this.views)
            .sold(this.sold)
            .createdAt(this.createdAt)
            .creator(creatorDto)
            .images(imageDtos)
            .tags(this.tags != null ? this.tags.stream().map(Tag::getName).toList() : List.of())
            .build();
    }

    public ArtworkSummaryDto toSummaryDto() {
        AccountSummaryDto creatorDto = creator != null ? creator.toSummaryDto() : null;
        ArtworkImage mainImage = images == null
            ? null
            : images.stream()
                .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                .filter(ArtworkImage::isMainImage)
                .findFirst()
                .orElseGet(() -> images.isEmpty() ? null : images.get(0));

        return ArtworkSummaryDto.builder()
            .id(this.id)
            .title(this.title)
            .imageUrl(mainImage != null ? mainImage.getBlobName() : null)
            .thumbnailUrl(mainImage != null ? mainImage.getThumbnailBlobName() : null)
            .price(this.price)
            .year(this.year)
            .views(this.views)
            .sold(this.sold)
            .createdAt(this.createdAt)
            .creator(creatorDto)
            .tags(this.tags != null ? this.tags.stream().map(Tag::getName).collect(Collectors.toList()) : List.of())
            .build();
    }

    // Validation that is not handled by JPA anymore - needs to be implemented elsewhere
    /*private void validateState() {
        if (title == null || title.trim().isEmpty()) throw new IllegalStateException("Artwork title is required");
        if (price == null) throw new IllegalStateException("Artwork price is required");
        if (creator == null) throw new IllegalStateException("Artwork requires a creator account");
    }*/
}
