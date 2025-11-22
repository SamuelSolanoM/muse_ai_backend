package com.muse_ai.logic.entity.painting;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaintingRepository extends JpaRepository<Painting, UUID> {

    List<Painting> findAllByOrderByUpdatedAtDesc();

    @Query("SELECT DISTINCT p FROM Painting p JOIN p.tags t WHERE LOWER(t) = LOWER(:tag)")
    List<Painting> findAllByTag(@Param("tag") String tag);

    Optional<Painting> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);
}
