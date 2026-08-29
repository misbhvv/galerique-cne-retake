package com.group2.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@ConfigurationProperties(prefix = "app.artwork-images")
public class ArtworkImageUploadProperties {

    private int maxImagesPerArtwork = 10;
    private long maxFileSizeBytes = 5L * 1024 * 1024;
    private long maxTotalUploadSizeBytes = 20L * 1024 * 1024;
    private int thumbnailMaxWidth = 600;
    private Set<String> allowedMimeTypes = Set.of("image/jpeg", "image/jpg", "image/png", "image/webp");

    public int getMaxImagesPerArtwork() {
        return maxImagesPerArtwork;
    }

    public void setMaxImagesPerArtwork(int maxImagesPerArtwork) {
        this.maxImagesPerArtwork = maxImagesPerArtwork;
    }

    public long getMaxFileSizeBytes() {
        return maxFileSizeBytes;
    }

    public void setMaxFileSizeBytes(long maxFileSizeBytes) {
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public long getMaxTotalUploadSizeBytes() {
        return maxTotalUploadSizeBytes;
    }

    public void setMaxTotalUploadSizeBytes(long maxTotalUploadSizeBytes) {
        this.maxTotalUploadSizeBytes = maxTotalUploadSizeBytes;
    }

    public int getThumbnailMaxWidth() {
        return thumbnailMaxWidth;
    }

    public void setThumbnailMaxWidth(int thumbnailMaxWidth) {
        this.thumbnailMaxWidth = thumbnailMaxWidth;
    }

    public Set<String> getAllowedMimeTypes() {
        return allowedMimeTypes;
    }

    public void setAllowedMimeTypes(Set<String> allowedMimeTypes) {
        this.allowedMimeTypes = allowedMimeTypes;
    }
}
