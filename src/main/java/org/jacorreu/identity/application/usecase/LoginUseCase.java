package org.jacorreu.identity.application.usecase;

import org.jacorreu.identity.application.dto.LoginRequest;
import org.jacorreu.identity.core.domain.RefreshTokenDomain;
import org.jacorreu.identity.core.gateway.JwtGateway;
import org.jacorreu.identity.core.gateway.PasswordEncoderGateway;
import org.jacorreu.identity.core.gateway.RefreshTokenRepository;
import org.jacorreu.identity.infra.dto.TokenPair;
import org.jacorreu.shared.validation.Notification;
import org.jacorreu.shared.validation.Result;
import org.jacorreu.user.core.domain.UserDomain;
import org.jacorreu.user.core.domain.valueobjects.Email;
import org.jacorreu.user.core.domain.valueobjects.Password;
import org.jacorreu.user.core.gateway.UserRepository;

import java.util.Optional;


public final class LoginUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoderGateway passwordEncoder;
    private final IssueTokenUseCase issueTokenUseCase;

    public LoginUseCase(UserRepository userRepository, PasswordEncoderGateway passwordEncoder, IssueTokenUseCase issueTokenUseCase) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.issueTokenUseCase = issueTokenUseCase;
    }

    public Result<TokenPair> execute(LoginRequest request) {
        Notification notification = new Notification();
        Email email = Email.restore(request.email());
        Password password = Password.restore(request.password());

        Optional<UserDomain> userOps = userRepository.findByEmail(email);

        if (userOps.isEmpty()) {
            notification.addError("Usuario nao existe! Crie uma conta");
            return Result.failure(notification);
        }

        UserDomain user = userOps.get();

        if (!passwordEncoder.matches(password.getValue(), user.getPassword().getValue())) {
            notification.addError("Email ou senha incorretos!");
            return Result.failure(notification);
        }

        return issueTokenUseCase.execute(user);
    }


}
