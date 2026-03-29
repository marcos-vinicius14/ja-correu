package org.jacorreu.identity.application.usecase;

import org.jacorreu.identity.application.dto.request.CreateUserRequest;
import org.jacorreu.identity.core.gateway.PasswordEncoderGateway;
import org.jacorreu.shared.validation.Notification;
import org.jacorreu.shared.validation.Result;
import org.jacorreu.user.core.domain.UserDomain;
import org.jacorreu.user.core.domain.valueobjects.Email;
import org.jacorreu.user.core.domain.valueobjects.Password;
import org.jacorreu.user.core.gateway.UserRepository;

public final class CreateUserUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoderGateway passwordEncoder;

    public CreateUserUseCase(UserRepository userRepository, PasswordEncoderGateway passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Result<Void> execute(CreateUserRequest request) {
        var notification = new Notification();
        var email = Email.restore(request.email());
        var password = Password.restore(request.password());

        return userRepository.existsByEmail(email)
                ? Result.failure(notification.addError("email", "Usuario ja existente! Realize o login!"))
                : createUser(request, email, password);
    }

    private Result<Void> createUser(CreateUserRequest request, Email email, Password password) {
        var encodedPassword = passwordEncoder.encode(password.getValue());

        var newUser = UserDomain.create(
                request.username(),
                email,
                Password.restore(encodedPassword)
        );

        userRepository.save(newUser);

        return Result.success();
    }
}
