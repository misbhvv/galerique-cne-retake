package com.group2.thumbnail;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ThumbnailJobTest {

    @Test
    void acceptsExpectedArtworkPaths() {
        ThumbnailJob job = new ThumbnailJob(
            "artworks/123/original.jpg",
            "artworks/123/thumbnails/original.jpg",
            300
        );

        assertDoesNotThrow(job::validate);
    }

    @Test
    void rejectsPathsOutsideTheArtworkPrefix() {
        ThumbnailJob job = new ThumbnailJob(
            "../private/file.jpg",
            "artworks/123/thumbnails/file.jpg",
            300
        );

        assertThrows(IllegalArgumentException.class, job::validate);
    }

    @Test
    void rejectsUnreasonableThumbnailWidth() {
        ThumbnailJob job = new ThumbnailJob(
            "artworks/123/original.jpg",
            "artworks/123/thumbnails/original.jpg",
            5000
        );

        assertThrows(IllegalArgumentException.class, job::validate);
    }
}
