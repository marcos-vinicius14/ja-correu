package org.jacorreu.identity.application.usecase;

import jakarta.persistence.EntityNotFoundException;
import org.jacorreu.identity.core.domain.RefreshTokenDomain;
import org.jacorreu.identity.core.gateway.RefreshTokenRepository;
import org.jacorreu.identity.infra.dto.TokenPair;
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

    public Result<TokenPair> execute(UUID token) {
        Notification notification = new Notification();
        RefreshTokenDomain tokenDomain = repository.findByTokenId(token).orElseThrow(() -> new EntityNotFoundException("Token nao encontrado"));

        if (!tokenDomain.isValid()) {
            notification.addError("Token invalido!");
            return Result.failure(notification);
        }

        repository.revoke(token);

        UserDomain user = userRepository.findById(tokenDomain.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario nao encontrado"));


        return issueTokenUseCase.execute(user);
    }

}
