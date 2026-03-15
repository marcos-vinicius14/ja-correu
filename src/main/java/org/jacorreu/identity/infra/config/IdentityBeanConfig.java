package org.jacorreu.identity.infra.config;

import org.jacorreu.identity.application.usecase.*;
import org.jacorreu.identity.core.gateway.ExtendsUserDetailsService;
import org.jacorreu.identity.core.gateway.JwtGateway;
import org.jacorreu.identity.core.gateway.PasswordEncoderGateway;
import org.jacorreu.identity.core.gateway.RefreshTokenRepository;
import org.jacorreu.user.core.gateway.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
public class IdentityBeanConfig {
    @Bean
    public IssueTokenUseCase issueTokenUseCase(
            JwtGateway jwtGateway,
            RefreshTokenRepository repository,
            @Value("${jwt.refresh.expiration}") long refreshExpirationSeconds
    ) {
        return new IssueTokenUseCase(jwtGateway, repository, refreshExpirationSeconds);
    }

    @Bean
    public LoginUseCase loginUseCase(
            UserRepository userRepository,
            PasswordEncoderGateway passwordEncoder,
            IssueTokenUseCase issueTokenUseCase
    ) {
        return new LoginUseCase(userRepository, passwordEncoder, issueTokenUseCase);
    }

    @Primary
    @Bean
    public ExtendsUserDetailsService customUserDetails(UserRepository repository) {
        return new CustomUserDetailsUseCase(repository);
    }

    @Bean
    public RenewTokenUseCase renewTokenUseCase(
            RefreshTokenRepository repository,
            UserRepository userRepository,
            IssueTokenUseCase issueTokenUseCase
    ) {
        return new RenewTokenUseCase(repository, userRepository, issueTokenUseCase);
    }

    @Bean
    public LogoutUseCase logoutUseCase(
            RefreshTokenRepository repository
    ) {
        return new LogoutUseCase(repository);
    }

    @Bean
    public CreateUserUseCase createUserUseCase(
            UserRepository userRepository,
            PasswordEncoderGateway passwordEncoder

    ) {
        return new CreateUserUseCase(userRepository, passwordEncoder);
    }

}
