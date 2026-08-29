package com.group2.backend.service;

import com.group2.backend.config.ArtworkImageUploadProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArtworkImageUploadPropertiesTest {

    @Test
    void usesSharpGalleryThumbnailWidthByDefault() {
        ArtworkImageUploadProperties properties = new ArtworkImageUploadProperties();

        assertEquals(600, properties.getThumbnailMaxWidth());
    }
}
