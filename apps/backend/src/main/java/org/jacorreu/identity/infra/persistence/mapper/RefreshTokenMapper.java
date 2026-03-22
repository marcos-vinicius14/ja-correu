package org.jacorreu.identity.infra.persistence.mapper;

import jakarta.persistence.EntityNotFoundException;
import org.jacorreu.identity.core.domain.RefreshTokenDomain;
import org.jacorreu.identity.infra.persistence.entity.RefreshTokenEntity;
import org.jacorreu.user.core.domain.UserDomain;
import org.jacorreu.user.core.gateway.UserRepository;
import org.jacorreu.user.infra.persistence.entity.UserJpaEntity;
import org.jacorreu.user.infra.persistence.mapper.UserMapper;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenMapper {
    private final UserRepository repository;
    private final UserMapper mapper;

    public RefreshTokenMapper(UserRepository repository, UserMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public RefreshTokenEntity toEntity(RefreshTokenDomain domain) {
        UserDomain user = repository.findById(domain.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario nao encontrado!"));

        UserJpaEntity userJpaEntity = mapper.toEntity(user);

        return new RefreshTokenEntity(
                domain.getTokenId(),
                domain.getToken(),
                domain.getExpirationDate(),
                domain.isRevoked(),
                userJpaEntity
        );
    }

    public RefreshTokenDomain toDomain(RefreshTokenEntity entity) {
        return RefreshTokenDomain.restore(
                entity.getTokenId(),
                entity.getToken(),
                entity.getExpirationDate(),
                entity.isRevoked(),
                entity.getUserJpaEntity().getId()
        );
    }
}
