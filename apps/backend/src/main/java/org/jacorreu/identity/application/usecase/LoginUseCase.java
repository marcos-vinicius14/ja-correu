package org.jacorreu.identity.application.usecase;

import org.jacorreu.identity.application.dto.request.LoginRequest;
import org.jacorreu.identity.core.gateway.PasswordEncoderGateway;
import org.jacorreu.identity.application.dto.response.TokenResponse;
import org.jacorreu.shared.validation.Notification;
import org.jacorreu.shared.validation.Result;
import org.jacorreu.user.core.domain.UserDomain;
import org.jacorreu.user.core.domain.valueobjects.Email;
import org.jacorreu.user.core.domain.valueobjects.Password;
import org.jacorreu.user.core.gateway.UserRepository;

public final class LoginUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoderGateway passwordEncoder;
    private final IssueTokenUseCase issueTokenUseCase;

    public LoginUseCase(UserRepository userRepository, PasswordEncoderGateway passwordEncoder, IssueTokenUseCase issueTokenUseCase) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.issueTokenUseCase = issueTokenUseCase;
    }

    public Result<TokenResponse> execute(LoginRequest request) {
        var notification = new Notification();
        var email = Email.restore(request.email());
        var password = Password.restore(request.password());

        return userRepository.findByEmail(email)
                .<Result<TokenResponse>>map(user -> validateCredentials(user, password, notification))
                .orElseGet(() -> {
                    notification.addError("credentials", "Usuario nao existe! Crie uma conta");
                    return Result.failure(notification);
                });
    }

    private Result<TokenResponse> validateCredentials(UserDomain user, Password password, Notification notification) {
        return passwordEncoder.matches(password.getValue(), user.getPassword().getValue())
                ? issueTokenUseCase.execute(user)
                : Result.failure(notification.addError("credentials", "Email ou senha incorretos!"));
    }
}
