package com.group2.backend.service;

import com.group2.backend.dto.TagSuggestionDto;
import com.group2.backend.model.Artwork;
import com.group2.backend.model.Tag;
import com.group2.backend.model.TagAlias;
import com.group2.backend.repository.ArtworkRepository;
import com.group2.backend.repository.TagAliasRepository;
import com.group2.backend.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class TagService {

    private static final int DEFAULT_POPULAR_TAG_LIMIT = 10;

    private final TagRepository tagRepository;
    private final TagAliasRepository tagAliasRepository;
    private final ArtworkRepository artworkRepository;

    @Caching(evict = {
        @CacheEvict(value = "tagSuggestions", allEntries = true),
        @CacheEvict(value = "popularTags", allEntries = true)
    })
    public List<Tag> resolveCanonicalTags(List<String> inputTags) {
        if (inputTags == null || inputTags.isEmpty()) return List.of();

        LinkedHashMap<String, Tag> canonicalTags = new LinkedHashMap<>();
        List<Tag> tagsToSave = new ArrayList<>();

        for (String raw : inputTags) {
            if (raw == null || raw.isBlank()) continue;

            String normalized = normalize(raw);
            if (normalized.isBlank()) continue;

            Tag resolved = resolveTag(raw, normalized);
            resolved.setUsageCount(Math.max(0, resolved.getUsageCount()) + 1);
            tagsToSave.add(resolved);
            canonicalTags.put(resolved.getId(), resolved);
        }

        tagRepository.saveAll(tagsToSave);
        return new ArrayList<>(canonicalTags.values());
    }

    public List<String> resolveCanonicalTagNames(List<String> inputTags) {
        return resolveCanonicalTags(inputTags).stream().map(Tag::getName).toList();
    }

    @Cacheable(value = "tagSuggestions", key = "#query == null ? '' : #query.trim()")
    @Transactional(readOnly = true)
    public List<TagSuggestionDto> suggestTags(String query) {
        String trimmed = query == null ? "" : query.trim();

        List<Tag> directMatches = trimmed.isBlank()
            ? tagRepository.findTop30ByOrderByUsageCountDescNameAsc()
            : tagRepository.findTop20ByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByUsageCountDescNameAsc(trimmed, trimmed);

        List<TagAlias> aliasMatches = trimmed.isBlank() ? List.of() : tagAliasRepository.findTop20ByAliasContainingIgnoreCase(trimmed);

        LinkedHashMap<String, Tag> unique = new LinkedHashMap<>();
        for (Tag tag : directMatches) {
            unique.put(tag.getId(), tag);
        }
        for (TagAlias alias : aliasMatches) {
            unique.put(alias.getTag().getId(), alias.getTag());
        }

        return unique.values().stream()
            .sorted(Comparator.comparingInt(Tag::getUsageCount).reversed().thenComparing(Tag::getName, String.CASE_INSENSITIVE_ORDER))
            .limit(20)
            .map(tag -> TagSuggestionDto.builder()
                .id(tag.getId())
                .name(tag.getName())
                .description(tag.getDescription())
                .usageCount(tag.getUsageCount())
                .build())
            .toList();
    }

    @Cacheable(value = "popularTags", key = "#limit == null ? 10 : #limit")
    @Transactional(readOnly = true)
    public List<TagSuggestionDto> popularTags(Integer limit) {
        int effectiveLimit = (limit == null || limit <= 0) ? DEFAULT_POPULAR_TAG_LIMIT : Math.min(limit, 50);
        List<Tag> seed = effectiveLimit <= 10
            ? tagRepository.findTop10ByOrderByUsageCountDescNameAsc()
            : tagRepository.findTop30ByOrderByUsageCountDescNameAsc();

        return seed.stream()
            .limit(effectiveLimit)
            .map(tag -> TagSuggestionDto.builder()
                .id(tag.getId())
                .name(tag.getName())
                .description(tag.getDescription())
                .usageCount(tag.getUsageCount())
                .build())
            .toList();
    }

    @Transactional(readOnly = true)
    public List<Artwork> findArtworksByTagQuery(String rawTagQuery) {
        if (rawTagQuery == null || rawTagQuery.isBlank()) {
            return List.of();
        }

        String normalized = normalize(rawTagQuery);
        if (normalized.isBlank()) {
            return List.of();
        }

        Optional<Tag> exact = tagRepository.findByNormalizedName(normalized);
        if (exact.isPresent()) {
            return artworkRepository.findByTagId(exact.get().getId());
        }

        Optional<TagAlias> alias = tagAliasRepository.findByNormalizedAlias(normalized);
        if (alias.isPresent()) {
            return artworkRepository.findByTagId(alias.get().getTag().getId());
        }

        Optional<Tag> fuzzy = findBestMatch(normalized);
        if (fuzzy.isPresent()) {
            return artworkRepository.findByTagId(fuzzy.get().getId());
        }

        List<Tag> matchingTags = tagRepository.findByNameContainingIgnoreCase(rawTagQuery.trim());
        return matchingTags.stream()
                .flatMap(tag -> artworkRepository.findByTagId(tag.getId()).stream())
                .distinct()
                .toList();
    }

    private Tag resolveTag(String raw, String normalized) {
        Optional<Tag> exact = tagRepository.findByNormalizedName(normalized);
        if (exact.isPresent()) {
            ensureAlias(raw, normalized, exact.get());
            return exact.get();
        }

        Optional<TagAlias> alias = tagAliasRepository.findByNormalizedAlias(normalized);
        if (alias.isPresent()) {
            return alias.get().getTag();
        }

        Optional<Tag> fuzzy = findBestMatch(normalized);
        if (fuzzy.isPresent()) {
            ensureAlias(raw, normalized, fuzzy.get());
            return fuzzy.get();
        }

        Tag created = tagRepository.save(Tag.builder()
            .name(prettyName(raw))
            .normalizedName(normalized)
            .description("Community tag used to classify artworks.")
            .usageCount(0)
            .build());

        ensureAlias(raw, normalized, created);
        return created;
    }

    private Optional<Tag> findBestMatch(String normalizedInput) {
        List<Tag> candidates = tagRepository.findTop30ByOrderByUsageCountDescNameAsc();
        Tag best = null;
        int bestDistance = Integer.MAX_VALUE;

        for (Tag candidate : candidates) {
            String normalizedTag = candidate.getNormalizedName();
            if (normalizedTag == null || normalizedTag.isBlank()) continue;

            int distance = levenshtein(normalizedInput, normalizedTag);
            int maxLen = Math.max(normalizedInput.length(), normalizedTag.length());
            double ratio = maxLen == 0 ? 0 : (double) distance / maxLen;

            if ((ratio <= 0.34 || normalizedTag.contains(normalizedInput) || normalizedInput.contains(normalizedTag)) && distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }

        return Optional.ofNullable(best);
    }

    private void ensureAlias(String rawAlias, String normalizedAlias, Tag tag) {
        if (normalizedAlias.equals(tag.getNormalizedName())) return;

        if (tagAliasRepository.findByNormalizedAlias(normalizedAlias).isPresent()) return;

        TagAlias alias = TagAlias.builder()
            .alias(prettyName(rawAlias))
            .normalizedAlias(normalizedAlias)
            .tag(tag)
            .build();

        tagAliasRepository.save(alias);
    }

    private String normalize(String input) {
        return input
            .trim()
            .toLowerCase(Locale.ROOT)
            .replaceAll("[^a-z0-9\\s-]", " ")
            .replaceAll("\\s+", "-")
            .replaceAll("-+", "-")
            .replaceAll("^-|-$", "");
    }

    private String prettyName(String input) {
        String normalized = normalize(input).replace('-', ' ');
        if (normalized.isBlank()) return "untagged";

        String[] words = normalized.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) continue;
            if (!sb.isEmpty()) sb.append(' ');
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return sb.toString();
    }

    private int levenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int cost = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                    Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                    dp[i - 1][j - 1] + cost
                );
            }
        }

        return dp[a.length()][b.length()];
    }
}
