package org.jacorreu.onboarding.infra.persistence.repository;

import org.jacorreu.onboarding.core.domain.AthleteProfileDomain;
import org.jacorreu.onboarding.core.gateway.AthleteProfileRepository;
import org.jacorreu.onboarding.infra.persistence.mapper.AthleteProfileMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class AthleteProfileRepositoryImpl implements AthleteProfileRepository {

    private final SpringDataAthleteProfileRepository repository;
    private final AthleteProfileMapper mapper;

    public AthleteProfileRepositoryImpl(SpringDataAthleteProfileRepository repository, AthleteProfileMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public void save(AthleteProfileDomain profile) {
        repository.save(mapper.toEntity(profile));
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return repository.existsByUserId(userId);
    }

    @Override
    public Optional<AthleteProfileDomain> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public void markEmbeddingGenerated(UUID id) {
        repository.markEmbeddingGenerated(id);
    }
}
