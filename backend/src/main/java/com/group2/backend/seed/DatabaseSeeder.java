package com.group2.backend.seed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group2.backend.model.Account;
import com.group2.backend.model.Artwork;
import com.group2.backend.model.ArtworkImage;
import com.group2.backend.model.ArtworkLike;
import com.group2.backend.model.Purchase;
import com.group2.backend.repository.AccountRepository;
import com.group2.backend.repository.ArtworkImageRepository;
import com.group2.backend.repository.ArtworkLikeRepository;
import com.group2.backend.repository.ArtworkRepository;
import com.group2.backend.repository.PurchaseRepository;
import com.group2.backend.repository.TokenRepository;
import com.group2.backend.config.ArtworkImageUploadProperties;
import com.group2.backend.service.ArtworkImageProcessingService;
import com.group2.backend.service.BlobStorageService;
import com.group2.backend.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private static final int MAX_IMAGES_PER_ARTWORK = 3;

    private final AccountRepository accountRepository;
    private final ArtworkRepository artworkRepository;
    private final ArtworkImageRepository artworkImageRepository;
    private final PurchaseRepository purchaseRepository;
    private final ArtworkLikeRepository artworkLikeRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final BlobStorageService blobStorageService;
    private final ArtworkImageProcessingService artworkImageProcessingService;
    private final ArtworkImageUploadProperties artworkImageUploadProperties;
    private final TagService tagService;
    private final ObjectMapper objectMapper;

    @Override
    public void run(String... args) throws Exception {
        List<AccountSeed> accountSeeds = readListFromResource("seed/accounts.json", new TypeReference<>() {});
        List<ArtworkSeed> artworkSeeds = readListFromResource("seed/artworks.json", new TypeReference<>() {});
        List<TagSeed> tagSeeds = readListFromResource("seed/tags.json", new TypeReference<>() {});

        Set<String> allowedTags = tagSeeds.stream().map(TagSeed::name).collect(Collectors.toSet());

        log.info("Cleaning database and blob storage (dev seeder)...");
        clearDatabase();

        Map<String, Account> accountMap = seedAccounts(accountSeeds);
        Map<String, Artwork> artworkMap = seedArtworks(artworkSeeds, accountMap, allowedTags);
        SeedStats stats = seedPurchasesAndLikes(accountSeeds, accountMap, artworkMap);

        log.info(
            "Database seed completed: {} accounts, {} artworks, {} purchases, {} likes",
            accountMap.size(),
            artworkMap.size(),
            stats.purchases(),
            stats.likes()
        );
    }

    private <T> List<T> readListFromResource(String classpathLocation, TypeReference<List<T>> type) throws Exception {
        ClassPathResource resource = new ClassPathResource(classpathLocation);
        return objectMapper.readValue(resource.getInputStream(), type);
    }

    private void clearDatabase() {
        artworkLikeRepository.deleteAll();
        purchaseRepository.deleteAll();
        artworkImageRepository.deleteAll();
        tokenRepository.deleteAll();
        artworkRepository.deleteAll();
        accountRepository.deleteAll();
        blobStorageService.deleteAll();
    }

    private Map<String, Account> seedAccounts(List<AccountSeed> accountSeeds) {
        Map<String, Account> result = new LinkedHashMap<>();

        for (AccountSeed seed : accountSeeds) {
            Account saved = accountRepository.save(Account.builder()
                .username(seed.username())
                .email(seed.email())
                .password(passwordEncoder.encode(seed.password() == null || seed.password().isBlank() ? "password123" : seed.password()))
                .build());
            result.put(saved.getUsername(), saved);
        }

        return result;
    }

    private Map<String, Artwork> seedArtworks(
        List<ArtworkSeed> artworkSeeds,
        Map<String, Account> accountMap,
        Set<String> allowedTags
    ) {
        Map<String, Artwork> result = new LinkedHashMap<>();

        for (int i = 0; i < artworkSeeds.size(); i++) {
            ArtworkSeed seed = artworkSeeds.get(i);

            if (result.containsKey(seed.id())) {
                throw new IllegalStateException("Duplicate artwork id in artworks.json: " + seed.id());
            }

            Account creator = accountMap.get(seed.creatorUsername());
            if (creator == null) {
                throw new IllegalStateException("Unknown creatorUsername for artwork " + seed.id() + ": " + seed.creatorUsername());
            }

            Set<String> missingTags = seed.tags().stream().filter(tag -> !allowedTags.contains(tag)).collect(Collectors.toSet());
            if (!missingTags.isEmpty()) {
                throw new IllegalStateException("Artwork " + seed.id() + " references unknown tags: " + missingTags);
            }

            Instant createdAt = Instant.now()
                .minus(Math.max(0, artworkSeeds.size() - i), ChronoUnit.DAYS)
                .minus((i * 3L) % 24L, ChronoUnit.HOURS);

            Artwork artwork = artworkRepository.save(Artwork.builder()
                .title(seed.title())
                .description(seed.description())
                .price(seed.price())
                .year(seed.year())
                .views(seed.views())
                .createdAt(createdAt)
                .creator(creator)
                .tags(tagService.resolveCanonicalTags(seed.tags()))
                .build());

            int uploadedImages = uploadSeedImages(artwork, seed);
            if (uploadedImages == 0) {
                artworkRepository.delete(artwork);
                log.warn("Skipping artwork {} because no usable images were found", seed.id());
                continue;
            }

            result.put(seed.id(), artwork);
            log.info("Seeded artwork {}/{}: {} ({} image(s), year={})", i + 1, artworkSeeds.size(), artwork.getTitle(), uploadedImages, artwork.getYear());
        }

        return result;
    }

    private int uploadSeedImages(Artwork artwork, ArtworkSeed seed) {
        int uploaded = 0;
        int limit = Math.min(MAX_IMAGES_PER_ARTWORK, seed.images().size());

        for (int i = 0; i < limit; i++) {
            String imagePath = seed.images().get(i);
            ClassPathResource resource = new ClassPathResource(imagePath);
            if (!resource.exists()) {
                log.warn("Missing image for artwork {}: {}", seed.id(), imagePath);
                continue;
            }

            try {
                byte[] bytes = resource.getInputStream().readAllBytes();
                String contentType = detectContentType(imagePath);

                ArtworkImageProcessingService.ImageMetadata meta = artworkImageProcessingService.extractMetadata(bytes);
                String ext = extensionForMime(contentType);

                String blobName = "artworks/" + artwork.getId() + "/seed-" + seed.id() + "-" + (i + 1) + ext;
                String thumbName = "artworks/" + artwork.getId() + "/thumbnails/seed-" + seed.id() + "-" + (i + 1) + ".jpg";

                blobStorageService.upload(blobName, bytes, contentType);
                byte[] thumb = artworkImageProcessingService.createThumbnail(
                    bytes,
                    artworkImageUploadProperties.getThumbnailMaxWidth(),
                    contentType
                );
                blobStorageService.upload(thumbName, thumb, "image/jpeg");

                String fileName = imagePath.substring(imagePath.lastIndexOf('/') + 1);
                artworkImageRepository.save(ArtworkImage.builder()
                    .artwork(artwork)
                    .blobName(blobName)
                    .thumbnailBlobName(thumbName)
                    .originalFileName(fileName)
                    .mimeType(contentType)
                    .fileSizeBytes(bytes.length)
                    .width(meta.getWidth())
                    .height(meta.getHeight())
                    .sortOrder(i)
                    .isMainImage(i == 0)
                    .createdAt(Instant.now())
                    .build());

                uploaded++;
            } catch (Exception ex) {
                log.warn("Failed to upload seed image {} for artwork {}: {}", imagePath, seed.id(), ex.getMessage());
            }
        }

        return uploaded;
    }

    private SeedStats seedPurchasesAndLikes(
        List<AccountSeed> accountSeeds,
        Map<String, Account> accountMap,
        Map<String, Artwork> artworkMap
    ) {
        int purchases = 0;
        int likes = 0;

        Set<String> purchaseKeys = new HashSet<>();
        Set<String> likeKeys = new HashSet<>();
        Map<String, Integer> likeOrder = new HashMap<>();

        for (AccountSeed accountSeed : accountSeeds) {
            Account account = accountMap.get(accountSeed.username());
            if (account == null) {
                continue;
            }

            List<String> purchased = accountSeed.purchasedArtworkIds() == null ? List.of() : accountSeed.purchasedArtworkIds();
            for (String artworkSeedId : purchased) {
                Artwork artwork = artworkMap.get(artworkSeedId);
                if (artwork == null || artwork.getCreator().getId().equals(account.getId())) {
                    continue;
                }

                if (artwork.isSold()) {
                    continue;
                }

                String key = account.getId() + ":" + artwork.getId();
                if (!purchaseKeys.add(key)) {
                    continue;
                }

                purchaseRepository.save(Purchase.builder()
                    .buyer(account)
                    .artwork(artwork)
                    .purchasePrice(artwork.getPrice())
                    .purchaseDate(artwork.getCreatedAt().plus(5L + purchases, ChronoUnit.DAYS))
                    .build());
                artwork.setSold(true);
                artworkRepository.save(artwork);
                purchases++;
            }

            List<String> liked = accountSeed.likedArtworkIds() == null ? List.of() : accountSeed.likedArtworkIds();
            for (String artworkSeedId : liked) {
                Artwork artwork = artworkMap.get(artworkSeedId);
                if (artwork == null || artwork.getCreator().getId().equals(account.getId())) {
                    continue;
                }

                String key = account.getId() + ":" + artwork.getId();
                if (!likeKeys.add(key)) {
                    continue;
                }

                int order = likeOrder.merge(account.getUsername(), 1, Integer::sum);
                artworkLikeRepository.save(ArtworkLike.builder()
                    .account(account)
                    .artwork(artwork)
                    .createdAt(artwork.getCreatedAt().plus(order, ChronoUnit.DAYS))
                    .build());
                likes++;
            }
        }

        log.info("Seeded purchases: {}", purchases);
        log.info("Seeded likes: {}", likes);

        return new SeedStats(purchases, likes);
    }

    private String detectContentType(String imagePath) {
        String lower = imagePath.toLowerCase();
        if (lower.endsWith(".png")) return "image/png";
        if (lower.endsWith(".webp")) return "image/webp";
        return "image/jpeg";
    }

    private String extensionForMime(String mimeType) {
        if ("image/png".equalsIgnoreCase(mimeType)) return ".png";
        if ("image/webp".equalsIgnoreCase(mimeType)) return ".webp";
        return ".jpg";
    }

    private record SeedStats(int purchases, int likes) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AccountSeed(
        String username,
        String email,
        String password,
        List<String> purchasedArtworkIds,
        List<String> likedArtworkIds
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ArtworkSeed(
        String id,
        String title,
        String artist,
        Integer year,
        String medium,
        List<String> images,
        String creatorUsername,
        int views,
        BigDecimal price,
        List<String> tags,
        String description
    ) {
    }

    public record TagSeed(
        String id,
        String name,
        String category
    ) {
    }
}

