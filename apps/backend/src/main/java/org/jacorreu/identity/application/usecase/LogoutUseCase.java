package org.jacorreu.identity.application.usecase;

import org.jacorreu.identity.core.gateway.JwtGateway;
import org.jacorreu.identity.core.gateway.RefreshTokenRepository;
import org.jacorreu.shared.validation.Result;

import java.util.UUID;

public final class LogoutUseCase {
    private final RefreshTokenRepository refreshToken;

    public LogoutUseCase(RefreshTokenRepository refreshToken) {
        this.refreshToken = refreshToken;
    }

    public Result<Void> execute(UUID userId) {
        refreshToken.revokeAllByUserId(userId);
        return Result.success();
    }
}
