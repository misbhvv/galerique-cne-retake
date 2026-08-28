package com.group2.backend.service;

import com.group2.backend.exception.service.ServiceException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Service
public class ArtworkImageProcessingService {

    @Getter
    @AllArgsConstructor
    public static class ImageMetadata {
        private final int width;
        private final int height;
    }

    public ImageMetadata extractMetadata(byte[] content) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(content));
            if (image == null) {
                throw new ServiceException("File is not a valid image", HttpStatus.BAD_REQUEST);
            }
            return new ImageMetadata(image.getWidth(), image.getHeight());
        } catch (ServiceException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ServiceException("Failed to process image content", HttpStatus.BAD_REQUEST);
        }
    }

    public byte[] createThumbnail(byte[] content, int maxWidth, String sourceMimeType) {
        String outputFormat = "image/png".equalsIgnoreCase(sourceMimeType) ? "png" : "jpg";

        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Thumbnails.of(new ByteArrayInputStream(content))
                .size(maxWidth, maxWidth)
                .keepAspectRatio(true)
                .outputFormat(outputFormat)
                .toOutputStream(output);
            return output.toByteArray();
        } catch (Exception ex) {
            throw new ServiceException("Failed to generate image thumbnail", HttpStatus.BAD_REQUEST);
        }
    }
}
