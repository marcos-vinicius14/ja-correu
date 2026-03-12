package org.jacorreu.identity.application.usecase;

import jakarta.persistence.EntityNotFoundException;
import org.jacorreu.identity.core.domain.RefreshTokenDomain;
import org.jacorreu.identity.core.gateway.JwtGateway;
import org.jacorreu.identity.core.gateway.RefreshTokenRepository;
import org.jacorreu.identity.infra.dto.TokenPair;
import org.jacorreu.shared.validation.Result;
import org.jacorreu.user.core.domain.UserDomain;
import org.jacorreu.user.core.gateway.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class RenewTokenUseCase {
    private final RefreshTokenRepository repository;
    private final JwtGateway jwtGateway;
    private final UserRepository userRepository;

    private  final long refreshExpirationSeconds;


    public RenewTokenUseCase(
            RefreshTokenRepository repository,
            JwtGateway jwtGateway,
            UserRepository userRepository,
            @Value("${jwt.refresh.expiration}")
            long refreshExpirationSeconds) {
        this.repository = repository;
        this.jwtGateway = jwtGateway;
        this.userRepository = userRepository;
        this.refreshExpirationSeconds = refreshExpirationSeconds;
    }

    public Result<TokenPair> renewToken(UUID token) {
        RefreshTokenDomain tokenDomain = repository.findByTokenId(token).orElseThrow(() -> new EntityNotFoundException("Token nao encontrado"));

        if (!tokenDomain.isValid()) {
            throw new IllegalArgumentException("Token invalido!");
        }

        repository.revoke(token);

        UserDomain user = userRepository.findById(tokenDomain.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario nao encontrado"));


        String accessToken = jwtGateway.generateToken(user.getId(), user.getEmail().getValue(), user.getName());

        String refreshTokenValue = UUID.randomUUID().toString();

        Instant expirationDate = Instant.now().plusSeconds(refreshExpirationSeconds);
        RefreshTokenDomain newRefreshToken = RefreshTokenDomain.create(refreshTokenValue, expirationDate, user.getId());

        repository.save(newRefreshToken);
        return Result.success(new TokenPair(accessToken, newRefreshToken.getToken()));
    }

}
