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
        Notification notification = new Notification();
        Email email = Email.restore(request.email());
        Password password = Password.restore(request.password());

        if (userRepository.existsByEmail(email)) {
            notification.addError("Usuario ja existente! Realize o login!");
            return Result.failure(notification);
        }

        String encodedPassword = passwordEncoder.encode(password.getValue());

        UserDomain newUser = UserDomain.create(
                request.username(),
                email,
                Password.restore(encodedPassword)
        );

        userRepository.save(newUser);

        return Result.success();
    }

}
