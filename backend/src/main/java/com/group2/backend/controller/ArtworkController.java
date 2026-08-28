package com.group2.backend.controller;

import com.group2.backend.dto.ArtworkCreateDto;
import com.group2.backend.dto.ArtworkDto;
import com.group2.backend.dto.ArtworkImageDto;
import com.group2.backend.dto.ArtworkImageReorderRequestDto;
import com.group2.backend.dto.ArtworkSummaryDto;
import com.group2.backend.dto.ArtworkUpdateDto;
import com.group2.backend.dto.LikeCountDto;
import com.group2.backend.service.ArtworkImageService;
import com.group2.backend.service.ArtworkLikeService;
import com.group2.backend.service.ArtworkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/artworks")
@RequiredArgsConstructor
@Tag(name = "artworks", description = "Artwork management endpoints")
public class ArtworkController {

    private final ArtworkService artworkService;
    private final ArtworkLikeService artworkLikeService;
    private final ArtworkImageService artworkImageService;

    @GetMapping
    @Operation(summary = "List artworks", description = "Paginated artworks")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK")
    })
    public ResponseEntity<Page<ArtworkSummaryDto>> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String sort
    ) {
        return ResponseEntity.ok(artworkService.listArtworks(page, size, sort));
    }

    @GetMapping("/search/tag")
    @Operation(summary = "Search artworks by tag", description = "Find artworks with a given tag")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK")
    })
    public ResponseEntity<List<ArtworkSummaryDto>> searchByTag(@RequestParam String tag) {
        return ResponseEntity.ok(artworkService.searchByTag(tag));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get artwork", description = "Get artwork by id and increment views")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ArtworkDto> getById(@PathVariable String id) {
        return ResponseEntity.ok(artworkService.getArtwork(id));
    }

    @PostMapping
    @Operation(summary = "Create artwork", description = "Create a new artwork", security = @SecurityRequirement(name = "bearer-jwt"))
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<ArtworkDto> create(@RequestBody ArtworkCreateDto body) {
        ArtworkDto created = artworkService.createArtwork(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update artwork", description = "Update existing artwork", security = @SecurityRequirement(name = "bearer-jwt"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ArtworkDto> update(@PathVariable String id, @RequestBody ArtworkUpdateDto body) {
        return ResponseEntity.ok(artworkService.updateArtwork(id, body));
    }

    @GetMapping("/{id}/images")
    @Operation(summary = "List artwork images", description = "List all images for an artwork in sort order")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<List<ArtworkImageDto>> listImages(@PathVariable String id) {
        return ResponseEntity.ok(artworkImageService.listImages(id));
    }

    @PostMapping(value = "/{id}/images", consumes = "multipart/form-data")
    @Operation(summary = "Upload artwork images", description = "Upload one or more images for an artwork", security = @SecurityRequirement(name = "bearer-jwt"))
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not found"),
        @ApiResponse(responseCode = "413", description = "Payload too large")
    })
    public ResponseEntity<List<ArtworkImageDto>> uploadImages(@PathVariable String id, @RequestPart("files") List<MultipartFile> files) {
        return ResponseEntity.status(HttpStatus.CREATED).body(artworkImageService.uploadImages(id, files));
    }

    @PutMapping("/{id}/images/{imageId}/main")
    @Operation(summary = "Set artwork main image", description = "Set the given image as the main image", security = @SecurityRequirement(name = "bearer-jwt"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<ArtworkImageDto> setMainImage(@PathVariable String id, @PathVariable String imageId) {
        return ResponseEntity.ok(artworkImageService.setMainImage(id, imageId));
    }

    @PutMapping("/{id}/images/order")
    @Operation(summary = "Reorder artwork images", description = "Persist image ordering using ordered image ids", security = @SecurityRequirement(name = "bearer-jwt"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<List<ArtworkImageDto>> reorderImages(@PathVariable String id, @RequestBody ArtworkImageReorderRequestDto body) {
        return ResponseEntity.ok(artworkImageService.reorderImages(id, body));
    }

    @DeleteMapping("/{id}/images/{imageId}")
    @Operation(summary = "Delete artwork image", description = "Delete an image and its thumbnail blob", security = @SecurityRequirement(name = "bearer-jwt"))
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Deleted"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> deleteImage(@PathVariable String id, @PathVariable String imageId) {
        artworkImageService.deleteImage(id, imageId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete artwork", description = "Delete existing artwork", security = @SecurityRequirement(name = "bearer-jwt"))
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Deleted"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> delete(@PathVariable String id) {
        artworkService.deleteArtwork(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Search artworks", description = "Search artworks by title")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK")
    })
    public ResponseEntity<List<ArtworkSummaryDto>> search(@RequestParam String query) {
        return ResponseEntity.ok(artworkService.search(query));
    }

    @GetMapping("/trending")
    @Operation(summary = "Trending artworks", description = "List trending artworks by views")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK")
    })
    public ResponseEntity<List<ArtworkSummaryDto>> trending() {
        return ResponseEntity.ok(artworkService.trending());
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "Like artwork", description = "Like an artwork", security = @SecurityRequirement(name = "bearer-jwt"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> like(@PathVariable String id) {
        artworkLikeService.like(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like")
    @Operation(summary = "Unlike artwork", description = "Remove like from artwork", security = @SecurityRequirement(name = "bearer-jwt"))
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Deleted"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<Void> unlike(@PathVariable String id) {
        artworkLikeService.unlike(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/likes")
    @Operation(summary = "Artwork likes", description = "Get like count for artwork")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<LikeCountDto> likeCount(@PathVariable String id) {
        long count = artworkLikeService.count(id);
        return ResponseEntity.ok(LikeCountDto.builder().count(count).build());
    }
}
