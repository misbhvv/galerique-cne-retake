package com.group2.backend.repository;

import com.group2.backend.model.TagAlias;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagAliasRepository extends MongoRepository<TagAlias, String> {
    Optional<TagAlias> findByNormalizedAlias(String normalizedAlias);

    List<TagAlias> findTop20ByAliasContainingIgnoreCase(String query);
}
