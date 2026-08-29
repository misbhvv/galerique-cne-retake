package com.group2.backend.service;

import com.group2.backend.dto.ArtworkCreateDto;
import com.group2.backend.dto.ArtworkDto;
import com.group2.backend.dto.ArtworkImageDto;
import com.group2.backend.dto.ArtworkSummaryDto;
import com.group2.backend.dto.ArtworkUpdateDto;
import com.group2.backend.exception.service.ServiceException;
import com.group2.backend.model.Account;
import com.group2.backend.model.Artwork;
import com.group2.backend.model.ArtworkImage;
import com.group2.backend.model.Tag;
import com.group2.backend.repository.ArtworkImageRepository;
import com.group2.backend.repository.ArtworkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ArtworkService {

    private final ArtworkRepository artworkRepository;
    private final ArtworkImageRepository artworkImageRepository;
    private final AuthService authService;
    private final BlobStorageService blobStorageService;
    private final ArtworkImageService artworkImageService;
    private final TagService tagService;

    @Cacheable(value = "artworkLists", key = "#page + ':' + #size + ':' + (#sort == null ? '' : #sort)")
    public Page<ArtworkSummaryDto> listArtworks(int page, int size, String sort) {
        Pageable pageable = buildPageable(page, size, sort);
        return artworkRepository.findAll(pageable).map(this::toSummaryDto);
    }

    public ArtworkDto getArtwork(String id) {
        Artwork artwork = artworkRepository.findById(id)
            .orElseThrow(() -> new ServiceException("Artwork not found", HttpStatus.NOT_FOUND));

        artwork.setViews(artwork.getViews() + 1);
        Artwork saved = artworkRepository.save(artwork);
        return toDto(saved);
    }

    @Caching(evict = {
        @CacheEvict(value = "accountArtworks", allEntries = true),
        @CacheEvict(value = "artworkLists", allEntries = true)
    })
    public ArtworkDto createArtwork(ArtworkCreateDto body) {
        validateCreateBody(body);
        Account creator = authService.getAccountFromRequest();

        Artwork toCreate = Artwork.builder()
            .title(body.getTitle().trim())
            .description(Optional.ofNullable(body.getDescription()).map(String::trim).orElse(null))
            .price(body.getPrice())
            .year(normalizeArtworkYear(body.getYear()))
            .tags(tagService.resolveCanonicalTags(body.getTags()))
            .creator(creator)
            .build();

        Artwork saved = artworkRepository.save(toCreate);
        return toDto(saved);
    }

    @Caching(evict = {
        @CacheEvict(value = "accountArtworks", allEntries = true),
        @CacheEvict(value = "artworkLists", allEntries = true)
    })
    public ArtworkDto updateArtwork(String id, ArtworkUpdateDto body) {
        Artwork artwork = artworkRepository.findById(id)
            .orElseThrow(() -> new ServiceException("Artwork not found", HttpStatus.NOT_FOUND));
        Account caller = authService.getAccountFromRequest();
        if (artwork.getCreator() == null || !caller.getId().equals(artwork.getCreator().getId())) {
            throw new ServiceException("Forbidden", HttpStatus.FORBIDDEN);
        }

        if (body.getTitle() != null && !body.getTitle().isBlank()) {
            artwork.setTitle(body.getTitle().trim());
        }
        if (body.getDescription() != null) {
            String trimmed = body.getDescription().trim();
            artwork.setDescription(trimmed.isEmpty() ? null : trimmed);
        }
        if (body.getPrice() != null) {
            artwork.setPrice(body.getPrice());
        }
        if (body.getYear() != null) {
            artwork.setYear(normalizeArtworkYear(body.getYear()));
        }
        if (body.getTags() != null) {
            artwork.getTags().clear();
            artwork.getTags().addAll(tagService.resolveCanonicalTags(body.getTags()));
        }

        Artwork saved = artworkRepository.save(artwork);
        return toDto(saved);
    }

    @Caching(evict = {
        @CacheEvict(value = "accountArtworks", allEntries = true),
        @CacheEvict(value = "artworkLists", allEntries = true)
    })
    public void deleteArtwork(String id) {
        Artwork artwork = artworkRepository.findById(id)
            .orElseThrow(() -> new ServiceException("Artwork not found", HttpStatus.NOT_FOUND));
        Account caller = authService.getAccountFromRequest();
        if (artwork.getCreator() == null || !caller.getId().equals(artwork.getCreator().getId())) {
            throw new ServiceException("Forbidden", HttpStatus.FORBIDDEN);
        }
        artworkImageService.deleteAllImagesForArtwork(id);
        artworkRepository.delete(artwork);
    }

    public List<ArtworkSummaryDto> search(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        String normalizedQuery = normalizeSearchText(query);
        List<String> queryTokens = splitTokens(normalizedQuery);

        return artworkRepository.findAll()
            .stream()
            .map(artwork -> {
                int score = computeSearchScore(artwork, normalizedQuery, queryTokens);
                if (score <= 0) {
                    return null;
                }
                ArtworkSummaryDto dto = toSummaryDto(artwork);
                dto.setSearchScore(score);
                return dto;
            })
            .filter(java.util.Objects::nonNull)
            .sorted(Comparator
                .comparing(ArtworkSummaryDto::getSearchScore, Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(ArtworkSummaryDto::getViews, Comparator.reverseOrder())
                .thenComparing(ArtworkSummaryDto::getCreatedAt, Comparator.reverseOrder()))
            .limit(80)
            .toList();
    }

    public List<ArtworkSummaryDto> searchByTag(String tag) {
        if (tag == null || tag.isBlank()) return List.of();
        return tagService.findArtworksByTagQuery(tag.trim())
            .stream()
            .sorted(Comparator.comparing(Artwork::getViews).reversed().thenComparing(Artwork::getCreatedAt).reversed())
            .map(this::toSummaryDto)
            .toList();
    }

    @Cacheable("trendingArtworks")
    public List<ArtworkSummaryDto> trending() {
        return artworkRepository.findTop10ByOrderByViewsDesc()
            .stream()
            .map(this::toSummaryDto)
            .toList();
    }

    @Cacheable(value = "accountArtworks", key = "#accountId")
    public List<ArtworkSummaryDto> listByAccount(String accountId) {
        return artworkRepository.findByCreatorId(accountId)
            .stream()
            .map(this::toSummaryDto)
            .toList();
    }

    private ArtworkDto toDto(Artwork artwork) {
        List<ArtworkImageDto> images = artworkImageRepository.findByArtworkIdOrderBySortOrderAsc(artwork.getId())
            .stream()
            .map(this::toImageDto)
            .toList();

        ArtworkImageDto mainImage = images.stream()
            .filter(ArtworkImageDto::isMainImage)
            .findFirst()
            .orElseGet(() -> images.isEmpty() ? null : images.get(0));

        return ArtworkDto.builder()
            .id(artwork.getId())
            .title(artwork.getTitle())
            .description(artwork.getDescription())
            .price(artwork.getPrice())
            .year(artwork.getYear())
            .views(artwork.getViews())
            .createdAt(artwork.getCreatedAt())
            .creator(artwork.getCreator() != null ? artwork.getCreator().toSummaryDto() : null)
            .imageUrl(mainImage != null ? mainImage.getUrl() : null)
            .thumbnailUrl(mainImage != null ? mainImage.getThumbnailUrl() : null)
            .images(images)
            .tags(artwork.getTags() != null ? artwork.getTags().stream().map(Tag::getName).toList() : List.of())
            .build();
    }

    private ArtworkSummaryDto toSummaryDto(Artwork artwork) {
        ArtworkImageDto mainImage = artworkImageRepository.findByArtworkIdAndIsMainImageTrue(artwork.getId())
            .map(this::toImageDto)
            .orElseGet(() -> artworkImageRepository.findByArtworkIdOrderBySortOrderAsc(artwork.getId())
                .stream()
                .findFirst()
                .map(this::toImageDto)
                .orElse(null));

        return ArtworkSummaryDto.builder()
            .id(artwork.getId())
            .title(artwork.getTitle())
            .price(artwork.getPrice())
            .year(artwork.getYear())
            .views(artwork.getViews())
            .createdAt(artwork.getCreatedAt())
            .creator(artwork.getCreator() != null ? artwork.getCreator().toSummaryDto() : null)
            .imageUrl(mainImage != null ? mainImage.getUrl() : null)
            .thumbnailUrl(mainImage != null ? mainImage.getThumbnailUrl() : null)
            .tags(artwork.getTags() != null ? artwork.getTags().stream().map(Tag::getName).toList() : List.of())
            .build();

    }

    private ArtworkImageDto toImageDto(ArtworkImage image) {
        return image.toDto(
            blobStorageService.toBlobUrl(image.getBlobName()),
            blobStorageService.toBlobUrl(image.getThumbnailBlobName())
        );
    }

    private Pageable buildPageable(int page, int size, String sort) {
        Sort sortSpec = parseSort(sort);
        return PageRequest.of(Math.max(page, 0), Math.max(size, 1), sortSpec);
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.unsorted();
        }

        String[] parts = sort.split(",");
        if (parts.length == 2) {
            Sort.Direction direction = Sort.Direction.fromOptionalString(parts[1].trim()).orElse(Sort.Direction.ASC);
            return Sort.by(direction, parts[0].trim());
        }

        return Sort.by(sort.trim());
    }

    private void validateCreateBody(ArtworkCreateDto body) {
        if (body == null) {
            throw new ServiceException("Artwork payload is required", HttpStatus.BAD_REQUEST);
        }
        if (body.getTitle() == null || body.getTitle().isBlank()) {
            throw new ServiceException("Artwork title is required", HttpStatus.BAD_REQUEST);
        }
        if (body.getPrice() == null) {
            throw new ServiceException("Artwork price is required", HttpStatus.BAD_REQUEST);
        }
    }

    private int computeSearchScore(Artwork artwork, String normalizedQuery, List<String> queryTokens) {
        String normalizedTitle = normalizeSearchText(artwork.getTitle());
        String normalizedDescription = normalizeSearchText(artwork.getDescription());
        String normalizedCreator = normalizeSearchText(artwork.getCreator() != null ? artwork.getCreator().getUsername() : "");
        String normalizedYear = artwork.getYear() == null ? "" : String.valueOf(artwork.getYear());
        String normalizedTags = artwork.getTags() == null
            ? ""
            : normalizeSearchText(String.join(" ", artwork.getTags().stream().map(Tag::getName).toList()));

        int score = 0;
        score += scoreField(normalizedQuery, queryTokens, normalizedTitle, 80, 26, 18, 16);
        score += scoreField(normalizedQuery, queryTokens, normalizedDescription, 28, 12, 8, 9);
        score += scoreField(normalizedQuery, queryTokens, normalizedTags, 44, 18, 12, 12);
        score += scoreField(normalizedQuery, queryTokens, normalizedCreator, 18, 10, 7, 8);
        score += scoreField(normalizedQuery, queryTokens, normalizedYear, 24, 14, 10, 0);

        if (score > 0) {
            score += Math.min(8, artwork.getViews() / 40);
        }

        return score;
    }

    private int scoreField(
        String normalizedQuery,
        List<String> queryTokens,
        String field,
        int exactWeight,
        int containsWeight,
        int perTokenWeight,
        int typoWeight
    ) {
        if (field == null || field.isBlank()) {
            return 0;
        }

        int score = 0;
        if (field.equals(normalizedQuery)) {
            score += exactWeight;
        }
        if (field.contains(normalizedQuery)) {
            score += containsWeight;
        }

        List<String> fieldTokens = splitTokens(field);
        int tokenScore = 0;
        for (String token : queryTokens) {
            if (token.isBlank()) {
                continue;
            }
            if (field.contains(token)) {
                tokenScore += perTokenWeight;
                continue;
            }

            if (hasTypoTolerantTokenMatch(token, fieldTokens)) {
                tokenScore += typoWeight;
            }
        }

        score += tokenScore;
        return score;
    }

    private boolean hasTypoTolerantTokenMatch(String queryToken, List<String> fieldTokens) {
        int maxDistance = queryToken.length() <= 4 ? 1 : 2;
        for (String fieldToken : fieldTokens) {
            if (fieldToken.isBlank()) {
                continue;
            }
            int distance = levenshtein(queryToken, fieldToken);
            if (distance <= maxDistance) {
                return true;
            }
            if (fieldToken.contains(queryToken) || queryToken.contains(fieldToken)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeSearchText(String value) {
        if (value == null) {
            return "";
        }
        return value
            .trim()
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9\\s-]", " ")
            .replaceAll("\\s+", " ");
    }

    private List<String> splitTokens(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        String[] raw = value.split(" ");
        List<String> result = new ArrayList<>(raw.length);
        for (String token : raw) {
            if (!token.isBlank()) {
                result.add(token);
            }
        }
        return result;
    }

    private int levenshtein(String left, String right) {
        int[][] dp = new int[left.length() + 1][right.length() + 1];
        for (int i = 0; i <= left.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= right.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= left.length(); i++) {
            for (int j = 1; j <= right.length(); j++) {
                int cost = left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                    Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[left.length()][right.length()];
    }

    private Integer normalizeArtworkYear(Integer year) {
        if (year == null) {
            return null;
        }
        return (year >= 1000 && year <= 2100) ? year : null;
    }
}
