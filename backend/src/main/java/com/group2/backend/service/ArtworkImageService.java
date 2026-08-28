package com.group2.backend.service;

import com.group2.backend.config.ArtworkImageUploadProperties;
import com.group2.backend.dto.ArtworkImageDto;
import com.group2.backend.dto.ArtworkImageReorderRequestDto;
import com.group2.backend.exception.service.ServiceException;
import com.group2.backend.model.Account;
import com.group2.backend.model.Artwork;
import com.group2.backend.model.ArtworkImage;
import com.group2.backend.repository.ArtworkImageRepository;
import com.group2.backend.repository.ArtworkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ArtworkImageService {

    private static final String THUMBNAIL_CONTENT_TYPE = "image/jpeg";

    private final ArtworkRepository artworkRepository;
    private final ArtworkImageRepository artworkImageRepository;
    private final AuthService authService;
    private final BlobStorageService blobStorageService;
    private final ArtworkImageProcessingService artworkImageProcessingService;
    private final ArtworkImageUploadProperties uploadProperties;

    @Transactional(readOnly = true)
    public List<ArtworkImageDto> listImages(String artworkId) {
        ensureArtworkExists(artworkId);
        return artworkImageRepository.findByArtworkIdOrderBySortOrderAsc(artworkId)
            .stream()
            .map(this::toDto)
            .toList();
    }

    public List<ArtworkImageDto> uploadImages(String artworkId, List<MultipartFile> files) {
        Artwork artwork = findArtwork(artworkId);
        verifyOwner(artwork);

        if (files == null || files.isEmpty()) {
            throw new ServiceException("At least one image file is required", HttpStatus.BAD_REQUEST);
        }

        long currentCount = artworkImageRepository.countByArtworkId(artworkId);
        if (currentCount + files.size() > uploadProperties.getMaxImagesPerArtwork()) {
            throw new ServiceException("Maximum number of images per artwork exceeded", HttpStatus.BAD_REQUEST);
        }

        long totalSize = files.stream().mapToLong(MultipartFile::getSize).sum();
        if (totalSize > uploadProperties.getMaxTotalUploadSizeBytes()) {
            throw new ServiceException("Total upload size exceeds the allowed limit", HttpStatus.PAYLOAD_TOO_LARGE);
        }

        List<String> uploadedBlobNames = new ArrayList<>();
        List<ArtworkImage> toPersist = new ArrayList<>();
        boolean hasMainImage = artworkImageRepository.findByArtworkIdAndIsMainImageTrue(artworkId).isPresent();
        int sortOrder = artworkImageRepository.findMaxSortOrderByArtworkId(artworkId)
                .stream()
                .findFirst()
                .map(img -> img.getSortOrder() + 1)
                .orElse(0);

        try {
            for (MultipartFile file : files) {
                validateFile(file);

                byte[] bytes = file.getBytes();
                ArtworkImageProcessingService.ImageMetadata metadata = artworkImageProcessingService.extractMetadata(bytes);

                String safeName = buildSafeFileName(file.getOriginalFilename());
                String baseId = UUID.randomUUID().toString();
                String blobName = "artworks/" + artworkId + "/" + baseId + "-" + safeName;
                String thumbName = "artworks/" + artworkId + "/thumbnails/" + baseId + "-" + thumbnailFileName(safeName);

                blobStorageService.upload(blobName, bytes, normalizeMimeType(file.getContentType()));
                uploadedBlobNames.add(blobName);

                byte[] thumbnail = artworkImageProcessingService.createThumbnail(
                    bytes,
                    uploadProperties.getThumbnailMaxWidth(),
                    normalizeMimeType(file.getContentType())
                );
                blobStorageService.upload(thumbName, thumbnail, THUMBNAIL_CONTENT_TYPE);
                uploadedBlobNames.add(thumbName);

                boolean makeMain = !hasMainImage && toPersist.isEmpty();
                ArtworkImage entity = ArtworkImage.builder()
                    .artwork(artwork)
                    .blobName(blobName)
                    .thumbnailBlobName(thumbName)
                    .originalFileName(safeName)
                    .mimeType(normalizeMimeType(file.getContentType()))
                    .fileSizeBytes(file.getSize())
                    .width(metadata.getWidth())
                    .height(metadata.getHeight())
                    .sortOrder(sortOrder++)
                    .isMainImage(makeMain)
                    .createdAt(Instant.now())
                    .build();

                toPersist.add(entity);
            }

            return artworkImageRepository.saveAll(toPersist)
                .stream()
                .map(this::toDto)
                .toList();
        } catch (IOException ex) {
            cleanupUploaded(uploadedBlobNames);
            throw new ServiceException("Failed to read uploaded file", HttpStatus.BAD_REQUEST);
        } catch (RuntimeException ex) {
            cleanupUploaded(uploadedBlobNames);
            throw ex;
        }
    }

    public ArtworkImageDto setMainImage(String artworkId, String imageId) {
        Artwork artwork = findArtwork(artworkId);
        verifyOwner(artwork);

        ArtworkImage target = artworkImageRepository.findByArtworkIdAndId(artworkId, imageId)
            .orElseThrow(() -> new ServiceException("Image not found", HttpStatus.NOT_FOUND));

        artworkImageRepository.findByArtworkIdAndIsMainImageTrue(artworkId)
            .ifPresent(currentMain -> {
                if (!Objects.equals(currentMain.getId(), target.getId())) {
                    currentMain.setMainImage(false);
                    artworkImageRepository.save(currentMain);
                }
            });

        target.setMainImage(true);
        return toDto(artworkImageRepository.save(target));
    }

    public List<ArtworkImageDto> reorderImages(String artworkId, ArtworkImageReorderRequestDto body) {
        Artwork artwork = findArtwork(artworkId);
        verifyOwner(artwork);

        if (body == null || body.getOrderedImageIds() == null || body.getOrderedImageIds().isEmpty()) {
            throw new ServiceException("orderedImageIds is required", HttpStatus.BAD_REQUEST);
        }

        List<ArtworkImage> images = artworkImageRepository.findByArtworkIdOrderBySortOrderAsc(artworkId);
        if (images.size() != body.getOrderedImageIds().size()) {
            throw new ServiceException("Reorder payload must include every artwork image exactly once", HttpStatus.BAD_REQUEST);
        }

        Set<String> uniqueIds = new HashSet<>(body.getOrderedImageIds());
        if (uniqueIds.size() != body.getOrderedImageIds().size()) {
            throw new ServiceException("orderedImageIds contains duplicates", HttpStatus.BAD_REQUEST);
        }

        Map<String, ArtworkImage> byId = new HashMap<>();
        images.forEach(img -> byId.put(img.getId(), img));

        for (String id : body.getOrderedImageIds()) {
            if (!byId.containsKey(id)) {
                throw new ServiceException("orderedImageIds contains unknown image id " + id, HttpStatus.BAD_REQUEST);
            }
        }

        for (int i = 0; i < body.getOrderedImageIds().size(); i++) {
            ArtworkImage image = byId.get(body.getOrderedImageIds().get(i));
            image.setSortOrder(i);
        }

        artworkImageRepository.saveAll(images);
        return artworkImageRepository.findByArtworkIdOrderBySortOrderAsc(artworkId)
            .stream()
            .map(this::toDto)
            .toList();
    }

    public void deleteImage(String artworkId, String imageId) {
        Artwork artwork = findArtwork(artworkId);
        verifyOwner(artwork);

        ArtworkImage image = artworkImageRepository.findByArtworkIdAndId(artworkId, imageId)
            .orElseThrow(() -> new ServiceException("Image not found", HttpStatus.NOT_FOUND));

        boolean wasMain = image.isMainImage();
        artworkImageRepository.delete(image);

        blobStorageService.deleteIfExists(image.getBlobName());
        blobStorageService.deleteIfExists(image.getThumbnailBlobName());

        List<ArtworkImage> remaining = artworkImageRepository.findByArtworkIdOrderBySortOrderAsc(artworkId);
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setSortOrder(i);
        }

        if (wasMain && !remaining.isEmpty()) {
            remaining.get(0).setMainImage(true);
        }

        artworkImageRepository.saveAll(remaining);
    }

    public void deleteAllImagesForArtwork(String artworkId) {
        List<ArtworkImage> images = artworkImageRepository.findByArtworkIdOrderBySortOrderAsc(artworkId);
        for (ArtworkImage image : images) {
            blobStorageService.deleteIfExists(image.getBlobName());
            blobStorageService.deleteIfExists(image.getThumbnailBlobName());
        }
    }

    private ArtworkImageDto toDto(ArtworkImage image) {
        return image.toDto(
            blobStorageService.toBlobUrl(image.getBlobName()),
            blobStorageService.toBlobUrl(image.getThumbnailBlobName())
        );
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("Empty files are not allowed", HttpStatus.BAD_REQUEST);
        }

        if (file.getSize() > uploadProperties.getMaxFileSizeBytes()) {
            throw new ServiceException("File exceeds the allowed per-file size limit", HttpStatus.PAYLOAD_TOO_LARGE);
        }

        String mimeType = normalizeMimeType(file.getContentType());
        if (!uploadProperties.getAllowedMimeTypes().contains(mimeType)) {
            throw new ServiceException("Unsupported image type. Allowed: jpeg, jpg, png, webp", HttpStatus.BAD_REQUEST);
        }
    }

    private String normalizeMimeType(String mimeType) {
        if (mimeType == null) {
            return "";
        }
        String normalized = mimeType.trim().toLowerCase(Locale.ROOT);
        if ("image/jpg".equals(normalized)) {
            return "image/jpeg";
        }
        return normalized;
    }

    private String buildSafeFileName(String originalFileName) {
        String raw = (originalFileName == null || originalFileName.isBlank()) ? "image" : originalFileName;
        String fileName = Path.of(raw).getFileName().toString();
        fileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "-");
        if (fileName.length() > 120) {
            fileName = fileName.substring(fileName.length() - 120);
        }
        if (fileName.isBlank()) {
            return "image";
        }
        return fileName;
    }

    private String thumbnailFileName(String safeName) {
        int lastDot = safeName.lastIndexOf('.');
        String nameWithoutExt = lastDot > 0 ? safeName.substring(0, lastDot) : safeName;
        return nameWithoutExt + ".jpg";
    }

    private void cleanupUploaded(List<String> uploadedBlobNames) {
        uploadedBlobNames.forEach(blobStorageService::deleteIfExists);
    }

    private void verifyOwner(Artwork artwork) {
        Account caller = authService.getAccountFromRequest();
        if (artwork.getCreator() == null || !caller.getId().equals(artwork.getCreator().getId())) {
            throw new ServiceException("Forbidden", HttpStatus.FORBIDDEN);
        }
    }

    private Artwork findArtwork(String artworkId) {
        return artworkRepository.findById(artworkId)
            .orElseThrow(() -> new ServiceException("Artwork not found", HttpStatus.NOT_FOUND));
    }

    private void ensureArtworkExists(String artworkId) {
        if (!artworkRepository.existsById(artworkId)) {
            throw new ServiceException("Artwork not found", HttpStatus.NOT_FOUND);
        }
    }
}
