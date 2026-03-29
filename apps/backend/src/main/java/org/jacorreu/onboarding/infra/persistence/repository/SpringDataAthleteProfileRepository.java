package org.jacorreu.onboarding.infra.persistence.repository;

import org.jacorreu.onboarding.infra.persistence.entity.AthleteProfileJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface SpringDataAthleteProfileRepository extends JpaRepository<AthleteProfileJpaEntity, UUID> {
    boolean existsByUserId(UUID userId);

    @Modifying
    @Query("UPDATE AthleteProfileJpaEntity e SET e.embeddingGenerated = true WHERE e.id = :id")
    void markEmbeddingGenerated(@Param("id") UUID id);
}
