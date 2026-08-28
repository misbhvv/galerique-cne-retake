package com.group2.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtworkImageDto {
    private String id;
    private String artworkId;
    private String blobName;
    private String originalFileName;
    private String mimeType;
    private long fileSizeBytes;
    private int width;
    private int height;
    private String thumbnailBlobName;
    private int sortOrder;
    private boolean isMainImage;
    private Instant createdAt;
    private String url;
    private String thumbnailUrl;
}
