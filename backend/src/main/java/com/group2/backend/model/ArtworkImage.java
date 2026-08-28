package com.group2.backend.model;

import com.group2.backend.dto.ArtworkImageDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "artwork_images")
@CompoundIndexes({
        @CompoundIndex(name = "idx_artwork_images_artwork_id", def = "{'artworkId': 1}"),
        @CompoundIndex(name = "idx_artwork_images_sort", def = "{'artworkId': 1, 'sortOrder': 1}"),
        @CompoundIndex(name = "idx_artwork_images_main", def = "{'artworkId': 1, 'isMainImage': 1}")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtworkImage {

    @Id
    private String id;

    @DBRef(lazy = true)
    @NotNull
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Artwork artwork;

    @NotBlank
    @Size(max = 500)
    private String blobName;

    @NotBlank
    @Size(max = 255)
    private String originalFileName;

    @NotBlank
    @Size(max = 100)
    private String mimeType;

    private long fileSizeBytes;

    private int width;

    private int height;

    @NotBlank
    @Size(max = 500)
    private String thumbnailBlobName;

    private int sortOrder;

    @Builder.Default
    private boolean isMainImage = false;

    @CreatedDate
    private Instant createdAt;

    public ArtworkImageDto toDto(String imageUrl, String thumbnailUrl) {
        return ArtworkImageDto.builder()
            .id(this.id)
            .artworkId(this.artwork != null ? this.artwork.getId() : null)
            .blobName(this.blobName)
            .originalFileName(this.originalFileName)
            .mimeType(this.mimeType)
            .fileSizeBytes(this.fileSizeBytes)
            .width(this.width)
            .height(this.height)
            .thumbnailBlobName(this.thumbnailBlobName)
            .sortOrder(this.sortOrder)
            .isMainImage(this.isMainImage)
            .createdAt(this.createdAt)
            .url(imageUrl)
            .thumbnailUrl(thumbnailUrl)
            .build();
    }
}
