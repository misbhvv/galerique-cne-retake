package com.group2.backend.repository;

import com.group2.backend.model.Artwork;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtworkRepository extends MongoRepository<Artwork, String> {
	List<Artwork> findByTitleContainingIgnoreCase(String query);

	@Query("{ 'tags.$id': ?0 }")
	List<Artwork> findByTagId(@Param("tagId") String tagId);

	List<Artwork> findTop10ByOrderByViewsDesc();

	List<Artwork> findByCreatorId(String creatorId);
}
