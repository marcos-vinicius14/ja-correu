package org.jacorreu.identity.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jacorreu.identity.application.dto.request.LoginRequest;
import org.jacorreu.identity.application.dto.response.TokenResponse;
import org.jacorreu.identity.core.gateway.PasswordEncoderGateway;
import org.jacorreu.shared.validation.Result;
import org.jacorreu.user.core.domain.UserDomain;
import org.jacorreu.user.core.domain.valueobjects.Email;
import org.jacorreu.user.core.gateway.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoderGateway passwordEncoder;

    @Mock
    private IssueTokenUseCase issueTokenUseCase;

    @InjectMocks
    private LoginUseCase loginUseCase;

    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password123";

    private UserDomain buildUser() {
        return UserDomain.restore(
                UUID.randomUUID(), "testuser", EMAIL, "encodedPassword", null, null, null
        );
    }

    @Test
    void execute_success_returnsTokens() {
        UserDomain user = buildUser();
        TokenResponse expectedToken = new TokenResponse("accessToken", "refreshToken");

        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(issueTokenUseCase.execute(user)).thenReturn(Result.success(expectedToken));

        Result<TokenResponse> result = loginUseCase.execute(new LoginRequest(EMAIL, PASSWORD));

        assertTrue(result.isSuccess());
        assertEquals("accessToken", result.getData().accessToken());
        assertEquals("refreshToken", result.getData().refreshToken());
        verify(issueTokenUseCase, times(1)).execute(user);
    }

    @Test
    void execute_userNotFound_returnsFailure() {
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());

        Result<TokenResponse> result = loginUseCase.execute(new LoginRequest(EMAIL, PASSWORD));

        assertFalse(result.isSuccess());
        assertEquals("Usuario nao existe! Crie uma conta",
                result.getNotification().getErrors().getFirst().message());
        verify(issueTokenUseCase, never()).execute(any());
    }

    @Test
    void execute_wrongPassword_returnsFailure() {
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(buildUser()));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        Result<TokenResponse> result = loginUseCase.execute(new LoginRequest(EMAIL, "wrongPassword"));

        assertFalse(result.isSuccess());
        assertEquals("Email ou senha incorretos!",
                result.getNotification().getErrors().getFirst().message());
        verify(issueTokenUseCase, never()).execute(any());
    }
}
