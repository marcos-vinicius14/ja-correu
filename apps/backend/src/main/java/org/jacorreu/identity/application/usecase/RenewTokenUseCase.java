package org.jacorreu.identity.application.usecase;

import org.jacorreu.identity.core.domain.RefreshTokenDomain;
import org.jacorreu.identity.core.gateway.RefreshTokenRepository;
import org.jacorreu.identity.application.dto.response.TokenResponse;
import org.jacorreu.shared.validation.Notification;
import org.jacorreu.shared.validation.Result;
import org.jacorreu.user.core.domain.UserDomain;
import org.jacorreu.user.core.gateway.UserRepository;

import java.util.UUID;

public final class RenewTokenUseCase {
    private final RefreshTokenRepository repository;
    private final UserRepository userRepository;
    private final IssueTokenUseCase issueTokenUseCase;

    public RenewTokenUseCase(
            RefreshTokenRepository repository,
            UserRepository userRepository,
            IssueTokenUseCase issueTokenUseCase) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.issueTokenUseCase = issueTokenUseCase;
    }

    public Result<TokenResponse> execute(UUID token) {
        var notification = new Notification();

        return repository.findByTokenId(token)
                .<Result<TokenResponse>>map(t -> validateAndRenewToken(t, notification))
                .orElseGet(() -> {
                    notification.addError("token", "Token nao encontrado");
                    return Result.failure(notification);
                });
    }

    private Result<TokenResponse> validateAndRenewToken(RefreshTokenDomain tokenDomain, Notification notification) {
        if (!tokenDomain.isValid()) {
            notification.addError("token", "Token invalido!");
            return Result.failure(notification);
        }
        repository.revoke(tokenDomain.getTokenId());
        return renewUserToken(tokenDomain.getUserId());
    }

    private Result<TokenResponse> renewUserToken(UUID userId) {
        return userRepository.findById(userId)
                .<Result<TokenResponse>>map(issueTokenUseCase::execute)
                .orElseGet(() -> {
                    var notification = new Notification();
                    notification.addError("user", "Usuario nao encontrado");
                    return Result.failure(notification);
                });
    }
}
