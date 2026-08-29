package com.group2.backend.dto;

public record ThumbnailJobMessage(
    String sourceBlobName,
    String thumbnailBlobName,
    int maxWidth
) {
}
