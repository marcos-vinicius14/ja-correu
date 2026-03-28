package org.jacorreu.user.infra.persistence.mapper;

import org.jacorreu.user.core.domain.UserDomain;
import org.jacorreu.user.core.domain.valueobjects.ValidStravaToken;
import org.jacorreu.user.infra.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDomain toDomain(UserJpaEntity jpaEntity) {
        return UserDomain.restore(
                jpaEntity.getId(),
                jpaEntity.getName(),
                jpaEntity.getEmail(),
                jpaEntity.getPassword(),
                jpaEntity.getStravaTokenAcessToken(),
                jpaEntity.getStravaRefreshToken(),
                jpaEntity.getStrava_expires_at(),
                jpaEntity.getStatus()
        );
    }

    public UserJpaEntity toEntity(UserDomain domain) {
        String stravaAccessToken = null;
        String stravaRefreshToken = null;
        Long stravaExpiresAt = null;

        if (domain.getStravaToken() instanceof ValidStravaToken token) {
            stravaAccessToken = token.accessToken();
            stravaRefreshToken = token.refreshToken();
            stravaExpiresAt = token.expiresAt();
        }

        return new UserJpaEntity(
                domain.getId(),
                domain.getName(),
                domain.getPassword().getValue(),
                domain.getEmail().getValue(),
                stravaAccessToken,
                stravaRefreshToken,
                stravaExpiresAt,
                domain.getStatus()
        );
    }
}
