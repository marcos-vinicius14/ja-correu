package org.jacorreu.identity.application.usecase;

import org.jacorreu.identity.core.domain.RefreshTokenDomain;
import org.jacorreu.identity.core.gateway.JwtGateway;
import org.jacorreu.identity.core.gateway.RefreshTokenRepository;
import org.jacorreu.identity.infra.dto.TokenPair;
import org.jacorreu.shared.validation.Result;
import org.jacorreu.user.core.domain.UserDomain;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.util.UUID;

public class IssueTokenUseCase {
    private final JwtGateway jwt;
    private final RefreshTokenRepository refreshTokenRepository;

    private final long refreshExpirationSeconds;

    public IssueTokenUseCase(
            JwtGateway jwt,
            RefreshTokenRepository refreshTokenRepository,
            @Value("${jwt.refresh.expiration}")
            long refreshExpirationSeconds
            ) {
        this.jwt = jwt;
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshExpirationSeconds = refreshExpirationSeconds;
    }

    public Result<TokenPair> execute(UserDomain user) {
        String accessToken = jwt.generateToken(user.getId(), user.getEmail().getValue(), user.getName());

        String refreshTokenValue = UUID.randomUUID().toString();

        Instant expirationDate = Instant.now().plusSeconds(refreshExpirationSeconds);
        RefreshTokenDomain newRefreshToken = RefreshTokenDomain.create(refreshTokenValue, expirationDate, user.getId());

        refreshTokenRepository.save(newRefreshToken);
        return Result.success(new TokenPair(accessToken, newRefreshToken.getToken()));
    }
}
