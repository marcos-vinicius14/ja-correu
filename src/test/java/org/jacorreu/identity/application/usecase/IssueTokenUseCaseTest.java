package org.jacorreu.identity.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jacorreu.identity.application.dto.response.TokenResponse;
import org.jacorreu.identity.core.domain.RefreshTokenDomain;
import org.jacorreu.identity.core.gateway.JwtGateway;
import org.jacorreu.identity.core.gateway.RefreshTokenRepository;
import org.jacorreu.shared.validation.Result;
import org.jacorreu.user.core.domain.UserDomain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class IssueTokenUseCaseTest {

    @Mock
    private JwtGateway jwt;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private IssueTokenUseCase issueTokenUseCase;

    private static final long REFRESH_EXPIRATION_SECONDS = 604800L;
    private static final String EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        issueTokenUseCase = new IssueTokenUseCase(jwt, refreshTokenRepository, REFRESH_EXPIRATION_SECONDS);
    }

    private UserDomain buildUser() {
        return UserDomain.restore(
                UUID.randomUUID(), "testuser", EMAIL, "encodedPassword", null, null, null
        );
    }

    @Test
    void execute_success_returnsTokenResponse() {
        UserDomain user = buildUser();
        when(jwt.generateToken(any(UUID.class), anyString(), anyString())).thenReturn("generatedAccessToken");

        Result<TokenResponse> result = issueTokenUseCase.execute(user);

        assertTrue(result.isSuccess());
        assertEquals("generatedAccessToken", result.getData().accessToken());
        assertNotNull(result.getData().refreshToken());
    }

    @Test
    void execute_savesRefreshTokenWithCorrectUserId() {
        UserDomain user = buildUser();
        when(jwt.generateToken(any(UUID.class), anyString(), anyString())).thenReturn("accessToken");

        issueTokenUseCase.execute(user);

        ArgumentCaptor<RefreshTokenDomain> captor = ArgumentCaptor.forClass(RefreshTokenDomain.class);
        verify(refreshTokenRepository, times(1)).save(captor.capture());

        RefreshTokenDomain saved = captor.getValue();
        assertEquals(user.getId(), saved.getUserId());
        assertFalse(saved.isRevoked());
        assertFalse(saved.isExpired());
    }

    @Test
    void execute_returnedRefreshTokenMatchesSavedToken() {
        UserDomain user = buildUser();
        when(jwt.generateToken(any(UUID.class), anyString(), anyString())).thenReturn("accessToken");

        Result<TokenResponse> result = issueTokenUseCase.execute(user);

        ArgumentCaptor<RefreshTokenDomain> captor = ArgumentCaptor.forClass(RefreshTokenDomain.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertEquals(captor.getValue().getToken(), result.getData().refreshToken());
    }
}
