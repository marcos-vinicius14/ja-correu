package org.jacorreu.user.infraestructure.persistence.mapper;

import org.jacorreu.user.core.domain.UserDomain;
import org.jacorreu.user.core.domain.valueobjects.ValidStravaToken;
import org.jacorreu.user.infraestructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDomain toDomain(UserJpaEntity jpaEntity) {
        return UserDomain.restore(
                jpaEntity.getId(),
                jpaEntity.getName(),
                jpaEntity.getEmail(),
                jpaEntity.getPassword(),
                jpaEntity.getStravaRefreshToken(),
                jpaEntity.getStravaTokenAcessToken(),
                jpaEntity.getStrava_expires_at()
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
                domain.getEmail().getValue(),
                domain.getPassword().getValue(),
                stravaAccessToken,
                stravaRefreshToken,
                stravaExpiresAt
        );
    }
}
