package com.group2.thumbnail;

public record ThumbnailJob(
    String sourceBlobName,
    String thumbnailBlobName,
    int maxWidth
) {
    public void validate() {
        if (!isArtworkPath(sourceBlobName)) {
            throw new IllegalArgumentException("sourceBlobName must be an artwork blob path");
        }
        if (!isArtworkPath(thumbnailBlobName)) {
            throw new IllegalArgumentException("thumbnailBlobName must be an artwork blob path");
        }
        if (sourceBlobName.equals(thumbnailBlobName)) {
            throw new IllegalArgumentException("source and thumbnail blob names must be different");
        }
        if (maxWidth < 32 || maxWidth > 2000) {
            throw new IllegalArgumentException("maxWidth must be between 32 and 2000 pixels");
        }
    }

    private static boolean isArtworkPath(String value) {
        return value != null && value.startsWith("artworks/") && !value.contains("..");
    }
}
