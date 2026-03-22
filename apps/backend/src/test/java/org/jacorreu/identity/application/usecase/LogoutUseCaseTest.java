package org.jacorreu.identity.application.usecase;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.jacorreu.identity.core.gateway.RefreshTokenRepository;
import org.jacorreu.shared.validation.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private LogoutUseCase logoutUseCase;

    @Test
    void execute_revokesAllTokensAndReturnsSuccess() {
        UUID userId = UUID.randomUUID();

        Result<Void> result = logoutUseCase.execute(userId);

        assertTrue(result.isSuccess());
        verify(refreshTokenRepository, times(1)).revokeAllByUserId(userId);
    }
}
