package com.group2.backend.service;

import com.group2.backend.exception.service.ServiceException;
import com.group2.backend.model.Account;
import com.group2.backend.model.Artwork;
import com.group2.backend.model.ArtworkLike;
import com.group2.backend.repository.ArtworkLikeRepository;
import com.group2.backend.repository.ArtworkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ArtworkLikeService {

    private final ArtworkRepository artworkRepository;
    private final ArtworkLikeRepository artworkLikeRepository;
    private final AuthService authService;

    @CacheEvict(value = "likeCounts", key = "#artworkId")
    public void like(String artworkId) {
        Artwork artwork = artworkRepository.findById(artworkId)
            .orElseThrow(() -> new ServiceException("Artwork not found", HttpStatus.NOT_FOUND));
        Account account = authService.getAccountFromRequest();

        boolean alreadyLiked = artworkLikeRepository.findByAccountIdAndArtworkId(account.getId(), artworkId).isPresent();
        if (alreadyLiked) {
            return;
        }

        ArtworkLike like = ArtworkLike.builder()
            .account(account)
            .artwork(artwork)
            .build();
        artworkLikeRepository.save(like);
    }

    @CacheEvict(value = "likeCounts", key = "#artworkId")
    public void unlike(String artworkId) {
        artworkRepository.findById(artworkId)
            .orElseThrow(() -> new ServiceException("Artwork not found", HttpStatus.NOT_FOUND));
        Account account = authService.getAccountFromRequest();
        artworkLikeRepository.deleteByAccountIdAndArtworkId(account.getId(), artworkId);
    }

    @Cacheable(value = "likeCounts", key = "#artworkId")
    @Transactional(readOnly = true)
    public long count(String artworkId) {
        artworkRepository.findById(artworkId)
            .orElseThrow(() -> new ServiceException("Artwork not found", HttpStatus.NOT_FOUND));
        return artworkLikeRepository.countByArtworkId(artworkId);
    }
}
