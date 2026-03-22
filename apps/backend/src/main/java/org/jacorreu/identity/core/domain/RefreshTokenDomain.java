package org.jacorreu.identity.core.domain;

import com.fasterxml.uuid.Generators;

import java.time.Instant;
import java.util.UUID;

public final class RefreshTokenDomain {
    private final UUID tokenId;
    private final String token;
    private final Instant expirationDate;
    private final boolean isRevoked;
    private final UUID userId;


    private RefreshTokenDomain(UUID tokenId, String token, Instant expirationDate, boolean isRevoked, UUID userId) {
        this.tokenId = tokenId;
        this.token = token;
        this.expirationDate = expirationDate;
        this.isRevoked = isRevoked;
        this.userId = userId;
    }

    public static RefreshTokenDomain restore(
            UUID tokenId,
            String token,
            Instant expirationDate,
            boolean isRevoked,
            UUID userid
    ) {
        return new RefreshTokenDomain(tokenId, token, expirationDate, isRevoked, userid);
    }

    public static RefreshTokenDomain create(
            String token,
            Instant expirationDate,
            UUID userId
    ) {
        UUID tokenId = Generators.timeBasedEpochGenerator().generate();

        return new RefreshTokenDomain(tokenId, token, expirationDate, false, userId);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expirationDate);
    }

    public boolean isRevoked() {
        return this.isRevoked;
    }

    public boolean isValid() {
        return !isExpired() && !isRevoked;
    }

    public UUID getUserId() {
        return userId;
    }

    public Instant getExpirationDate() {
        return expirationDate;
    }

    public String getToken() {
        return token;
    }

    public UUID getTokenId() {
        return tokenId;
    }
}
