package com.group2.thumbnail;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ThumbnailGeneratorTest {

    private final ThumbnailGenerator generator = new ThumbnailGenerator();

    @Test
    void keepsAspectRatioAndLimitsWidth() throws Exception {
        byte[] source = createImage(1200, 600);

        BufferedImage thumbnail = ImageIO.read(new ByteArrayInputStream(
            generator.createJpeg(source, 300)
        ));

        assertNotNull(thumbnail);
        assertEquals(300, thumbnail.getWidth());
        assertEquals(150, thumbnail.getHeight());
    }

    @Test
    void doesNotUpscaleSmallImages() throws Exception {
        byte[] source = createImage(120, 60);

        BufferedImage thumbnail = ImageIO.read(new ByteArrayInputStream(
            generator.createJpeg(source, 300)
        ));

        assertEquals(120, thumbnail.getWidth());
        assertEquals(60, thumbnail.getHeight());
    }

    @Test
    void rejectsInvalidImageContent() {
        assertThrows(
            IllegalArgumentException.class,
            () -> generator.createJpeg("not an image".getBytes(), 300)
        );
    }

    private byte[] createImage(int width, int height) throws Exception {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        var graphics = image.createGraphics();
        graphics.setColor(Color.BLUE);
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", output);
        return output.toByteArray();
    }
}
