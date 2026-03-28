package org.jacorreu.identity.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import org.jacorreu.identity.application.dto.response.TokenResponse;
import org.jacorreu.identity.core.domain.RefreshTokenDomain;
import org.jacorreu.identity.core.gateway.RefreshTokenRepository;
import org.jacorreu.shared.validation.Result;
import org.jacorreu.user.core.domain.UserDomain;
import org.jacorreu.user.core.gateway.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class RenewTokenUseCaseTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private IssueTokenUseCase issueTokenUseCase;

    @InjectMocks
    private RenewTokenUseCase renewTokenUseCase;

    private UserDomain buildUser(UUID userId) {
        return UserDomain.restore(userId, "testuser", "test@example.com", "encodedPassword", null, null, null);
    }

    private RefreshTokenDomain buildValidToken(UUID tokenId, UUID userId) {
        return RefreshTokenDomain.restore(tokenId, "tokenValue", Instant.now().plusSeconds(3600), false, userId);
    }

    private RefreshTokenDomain buildExpiredToken(UUID tokenId, UUID userId) {
        return RefreshTokenDomain.restore(tokenId, "tokenValue", Instant.now().minusSeconds(3600), false, userId);
    }

    private RefreshTokenDomain buildRevokedToken(UUID tokenId, UUID userId) {
        return RefreshTokenDomain.restore(tokenId, "tokenValue", Instant.now().plusSeconds(3600), true, userId);
    }

    @Test
    void execute_success_revokesOldTokenAndIssuesNew() {
        UUID tokenId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserDomain user = buildUser(userId);
        TokenResponse expectedTokens = new TokenResponse("newAccessToken", "newRefreshToken");

        when(refreshTokenRepository.findByTokenId(tokenId)).thenReturn(Optional.of(buildValidToken(tokenId, userId)));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(issueTokenUseCase.execute(user)).thenReturn(Result.success(expectedTokens));

        Result<TokenResponse> result = renewTokenUseCase.execute(tokenId);

        assertTrue(result.isSuccess());
        assertEquals("newAccessToken", result.getData().accessToken());
        verify(refreshTokenRepository, times(1)).revoke(tokenId);
        verify(issueTokenUseCase, times(1)).execute(user);
    }

    @Test
    void execute_tokenNotFound_returnsFailure() {
        UUID tokenId = UUID.randomUUID();

        when(refreshTokenRepository.findByTokenId(tokenId)).thenReturn(Optional.empty());

        Result<TokenResponse> result = renewTokenUseCase.execute(tokenId);

        assertFalse(result.isSuccess());
        assertEquals("Token nao encontrado", result.getNotification().getErrors().getFirst().message());
        verify(refreshTokenRepository, never()).revoke(any());
        verify(issueTokenUseCase, never()).execute(any());
    }

    @Test
    void execute_expiredToken_returnsFailure() {
        UUID tokenId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(refreshTokenRepository.findByTokenId(tokenId))
                .thenReturn(Optional.of(buildExpiredToken(tokenId, userId)));

        Result<TokenResponse> result = renewTokenUseCase.execute(tokenId);

        assertFalse(result.isSuccess());
        assertEquals("Token invalido!", result.getNotification().getErrors().getFirst().message());
        verify(refreshTokenRepository, never()).revoke(any());
        verify(issueTokenUseCase, never()).execute(any());
    }

    @Test
    void execute_revokedToken_returnsFailure() {
        UUID tokenId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(refreshTokenRepository.findByTokenId(tokenId))
                .thenReturn(Optional.of(buildRevokedToken(tokenId, userId)));

        Result<TokenResponse> result = renewTokenUseCase.execute(tokenId);

        assertFalse(result.isSuccess());
        assertEquals("Token invalido!", result.getNotification().getErrors().getFirst().message());
        verify(refreshTokenRepository, never()).revoke(any());
        verify(issueTokenUseCase, never()).execute(any());
    }

    @Test
    void execute_userNotFound_returnsFailure() {
        UUID tokenId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(refreshTokenRepository.findByTokenId(tokenId))
                .thenReturn(Optional.of(buildValidToken(tokenId, userId)));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Result<TokenResponse> result = renewTokenUseCase.execute(tokenId);

        assertFalse(result.isSuccess());
        assertEquals("Usuario nao encontrado", result.getNotification().getErrors().getFirst().message());
        verify(refreshTokenRepository, times(1)).revoke(tokenId);
        verify(issueTokenUseCase, never()).execute(any());
    }
}
