package org.jacorreu.identity.infra.persistence.entity;

import jakarta.persistence.*;
import org.jacorreu.user.infra.persistence.entity.UserJpaEntity;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tb_refresh_token")
public class RefreshTokenEntity {
    @Id
    @Column(name = "token_id")
    private UUID tokenId;

    @Column(unique = true, nullable = false)
    private String token;

    @Column(name = "expiration_date", nullable = false)
    private Instant expirationDate;

    @Column(name = "is_revoked", nullable = false)
    private boolean isRevoked;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity userJpaEntity;

    protected RefreshTokenEntity() {

    }

    public RefreshTokenEntity(UUID tokenId, String token, Instant expirationDate, boolean isRevoked, UserJpaEntity userJpaEntity) {
        this.tokenId = tokenId;
        this.token = token;
        this.expirationDate = expirationDate;
        this.isRevoked = isRevoked;
        this.userJpaEntity = userJpaEntity;
    }

    public UUID getTokenId() {
        return tokenId;
    }

    public String getToken() {
        return token;
    }

    public Instant getExpirationDate() {
        return expirationDate;
    }

    public boolean isRevoked() {
        return isRevoked;
    }

    public UserJpaEntity getUserJpaEntity() {
        return userJpaEntity;
    }
}
