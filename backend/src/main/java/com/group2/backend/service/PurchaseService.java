package com.group2.backend.service;

import com.group2.backend.dto.PurchaseDto;
import com.group2.backend.dto.ArtworkSummaryDto;
import com.group2.backend.exception.service.ServiceException;
import com.group2.backend.model.Account;
import com.group2.backend.model.Artwork;
import com.group2.backend.model.ArtworkImage;
import com.group2.backend.model.Purchase;
import com.group2.backend.repository.ArtworkImageRepository;
import com.group2.backend.repository.ArtworkRepository;
import com.group2.backend.repository.PurchaseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final ArtworkRepository artworkRepository;
    private final ArtworkImageRepository artworkImageRepository;
    private final AuthService authService;
    private final BlobStorageService blobStorageService;

    public PurchaseDto createPurchase(String artworkId) {
        Artwork artwork = artworkRepository.findById(artworkId)
            .orElseThrow(() -> new ServiceException("Artwork not found", HttpStatus.NOT_FOUND));
        Account buyer = authService.getAccountFromRequest();

        if (artwork.getCreator() != null && buyer.getId().equals(artwork.getCreator().getId())) {
            throw new ServiceException("Cannot purchase your own artwork", HttpStatus.BAD_REQUEST);
        }

        if (artwork.isSold()) {
            throw new ServiceException("This artwork has already been sold", HttpStatus.CONFLICT);
        }

        Purchase purchase = Purchase.builder()
            .artwork(artwork)
            .buyer(buyer)
            .purchasePrice(artwork.getPrice())
            .build();

        Purchase saved = purchaseRepository.save(purchase);
        artwork.setSold(true);
        artworkRepository.save(artwork);
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<PurchaseDto> getMyPurchases() {
        Account buyer = authService.getAccountFromRequest();
        return purchaseRepository.findByBuyerId(buyer.getId())
            .stream()
            .map(this::toDto)
            .toList();
    }

    private PurchaseDto toDto(Purchase purchase) {
        Artwork artwork = purchase.getArtwork();
        ArtworkImage mainImage = artwork == null
            ? null
            : artworkImageRepository.findByArtworkIdAndIsMainImageTrue(artwork.getId())
                .orElseGet(() -> artworkImageRepository.findByArtworkIdOrderBySortOrderAsc(artwork.getId())
                    .stream()
                    .findFirst()
                    .orElse(null));

        ArtworkSummaryDto artworkDto = artwork == null ? null : ArtworkSummaryDto.builder()
            .id(artwork.getId())
            .title(artwork.getTitle())
            .price(artwork.getPrice())
            .views(artwork.getViews())
            .createdAt(artwork.getCreatedAt())
            .creator(artwork.getCreator() != null ? artwork.getCreator().toSummaryDto() : null)
            .imageUrl(mainImage != null ? blobStorageService.toBlobUrl(mainImage.getBlobName()) : null)
            .thumbnailUrl(mainImage != null ? blobStorageService.toBlobUrl(mainImage.getThumbnailBlobName()) : null)
            .build();

        return PurchaseDto.builder()
            .id(purchase.getId())
            .purchasePrice(purchase.getPurchasePrice())
            .purchaseDate(purchase.getPurchaseDate())
            .buyer(purchase.getBuyer() != null ? purchase.getBuyer().toSummaryDto() : null)
            .artwork(artworkDto)
            .build();
    }
}
