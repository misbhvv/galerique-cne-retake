package com.group2.backend.service;

import com.group2.backend.exception.service.ServiceException;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArtworkImageProcessingServiceTest {

    private final ArtworkImageProcessingService service = new ArtworkImageProcessingService();

    @Test
    void createsThumbnailWithOriginalAspectRatio() throws Exception {
        byte[] original = createImage(1200, 600);

        byte[] result = service.createThumbnail(original, 300, "image/jpeg");
        BufferedImage thumbnail = ImageIO.read(new ByteArrayInputStream(result));

        assertNotNull(thumbnail);
        assertEquals(300, thumbnail.getWidth());
        assertEquals(150, thumbnail.getHeight());
    }

    @Test
    void rejectsContentThatIsNotAnImage() {
        assertThrows(ServiceException.class, () -> service.extractMetadata("not an image".getBytes()));
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
