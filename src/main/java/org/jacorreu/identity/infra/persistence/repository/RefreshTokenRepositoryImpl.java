package org.jacorreu.identity.infra.persistence.repository;

import org.jacorreu.identity.core.domain.RefreshTokenDomain;
import org.jacorreu.identity.core.gateway.RefreshTokenRepository;
import org.jacorreu.identity.infra.persistence.entity.RefreshTokenEntity;
import org.jacorreu.identity.infra.persistence.mapper.RefreshTokenMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {
    private final SpringDataRefreshTokenRepository repository;
    private final RefreshTokenMapper mapper;


    public RefreshTokenRepositoryImpl(SpringDataRefreshTokenRepository repository, RefreshTokenMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public void save(RefreshTokenDomain domain) {
        RefreshTokenEntity refreshTokenToSave = mapper.toEntity(domain);
        repository.save(refreshTokenToSave);
    }

    @Override
    public Optional<RefreshTokenDomain> findByTokenId(UUID tokenId) {
        return repository.findByTokenId(tokenId)
                .map(mapper::toDomain);
    }

    @Override
    public void revoke(UUID tokenId) {
        repository.revokeToken(tokenId);
    }
}
