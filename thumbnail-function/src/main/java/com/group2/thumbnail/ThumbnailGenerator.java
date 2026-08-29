package com.group2.thumbnail;

import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ThumbnailGenerator {

    public byte[] createJpeg(byte[] source, int maxWidth) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(source));
        if (image == null) {
            throw new IllegalArgumentException("Source blob does not contain a supported image");
        }

        int targetWidth = Math.min(maxWidth, image.getWidth());
        int targetHeight = Math.max(1, (int) Math.round(
            image.getHeight() * (targetWidth / (double) image.getWidth())
        ));

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Thumbnails.of(image)
                .size(targetWidth, targetHeight)
                .keepAspectRatio(true)
                .outputFormat("jpg")
                .outputQuality(0.85)
                .toOutputStream(output);
            return output.toByteArray();
        }
    }
}
