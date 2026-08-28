package com.group2.backend.repository;

import com.group2.backend.model.ArtworkLike;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArtworkLikeRepository extends MongoRepository<ArtworkLike, String> {
    Optional<ArtworkLike> findByAccountIdAndArtworkId(String accountId, String artworkId);

    long countByArtworkId(String artworkId);

    void deleteByAccountIdAndArtworkId(String accountId, String artworkId);
}
