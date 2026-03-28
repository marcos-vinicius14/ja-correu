package org.jacorreu.onboarding.core.gateway;

import org.jacorreu.onboarding.core.domain.AthleteProfileDomain;

import java.util.Optional;
import java.util.UUID;

public interface AthleteProfileRepository {
    void save(AthleteProfileDomain profile);
    boolean existsByUserId(UUID userId);
    Optional<AthleteProfileDomain> findById(UUID id);
    void markEmbeddingGenerated(UUID id);
}
