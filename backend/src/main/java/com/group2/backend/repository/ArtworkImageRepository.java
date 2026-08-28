package com.group2.backend.repository;

import com.group2.backend.model.ArtworkImage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtworkImageRepository extends MongoRepository<ArtworkImage, String> {

    List<ArtworkImage> findByArtworkIdOrderBySortOrderAsc(String artworkId);

    Optional<ArtworkImage> findByArtworkIdAndIsMainImageTrue(String artworkId);

    Optional<ArtworkImage> findByArtworkIdAndId(String artworkId, String id);

    long countByArtworkId(String artworkId);

    @Query(value = "{ 'artworkId': ?0 }", sort = "{ 'sortOrder': -1 }")
    List<ArtworkImage> findMaxSortOrderByArtworkId(String artworkId);
}
