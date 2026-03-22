package org.jacorreu.identity.core.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class RefreshTokenDomainTest {

  @Test
  @DisplayName("Should restore a refresh token with valid properties")
  public void shouldRestoreRefreshTokenWithValidProperties() {
    UUID tokenId = UUID.randomUUID();
    String token = "sample-refresh-token";
    Instant expirationDate = Instant.now().plusSeconds(3600);
    boolean isRevoked = false;
    UUID userId = UUID.randomUUID();

    RefreshTokenDomain refreshToken = RefreshTokenDomain.restore(tokenId, token, expirationDate, isRevoked, userId);

    assertEquals(tokenId, refreshToken.getTokenId());
    assertEquals(token, refreshToken.getToken());
    assertEquals(expirationDate, refreshToken.getExpirationDate());
    assertEquals(userId, refreshToken.getUserId());

    assertFalse(refreshToken.isRevoked());
    assertFalse(refreshToken.isExpired());
    assertTrue(refreshToken.isValid());
  }

  @Test
  @DisplayName("Should create a new token with valid properties")
  public void shouldCreateNewTokenWithValidProperties() {

    String token = "new-refresh-token";
    Instant expirationDate = Instant.now().plusSeconds(3600);
    UUID userId = UUID.randomUUID();

    RefreshTokenDomain refreshToken = RefreshTokenDomain.create(token, expirationDate, userId);

    assertNotNull(refreshToken.getTokenId());
    assertEquals(userId, refreshToken.getUserId());
    assertEquals(expirationDate, refreshToken.getExpirationDate());
    assertEquals(false, refreshToken.isRevoked());
    assertEquals(false, refreshToken.isExpired());
    assertEquals(true, refreshToken.isValid());

  }

  @Test
  @DisplayName("Should return true for expired token")
  public void shouldReturnTrueForExpiredToken() {
    String token = "expired-refresh-token";
    Instant expirationDate = Instant.now().minusSeconds(3600);
    UUID userId = UUID.randomUUID();

    RefreshTokenDomain refreshToken = RefreshTokenDomain.create(token, expirationDate, userId);
    assertTrue(refreshToken.isExpired());
    assertFalse(refreshToken.isValid());
  }

  @Test
  @DisplayName("Should return false for non-expired token")
  public void shouldReturnFalseForNonExpiredToken() {
    String token = "valid-refresh-token";
    Instant expirationDate = Instant.now().plusSeconds(3600);
    UUID userId = UUID.randomUUID();

    RefreshTokenDomain refreshToken = RefreshTokenDomain.create(token, expirationDate, userId);

    assertFalse(refreshToken.isExpired());
    assertTrue(refreshToken.isValid());
  }

  @Test
  @DisplayName("Should return true for revoked token")
  public void shouldReturnTrueForRevokedToken() {
    String token = "revoked-refresh-token";
    Instant expirationDate = Instant.now().plusSeconds(3600);
    UUID userId = UUID.randomUUID();

    RefreshTokenDomain refreshToken = RefreshTokenDomain.restore(UUID.randomUUID(), token, expirationDate, true,
        userId);

    assertTrue(refreshToken.isRevoked());
    assertFalse(refreshToken.isValid());
  }

  @Test
  @DisplayName("Should return false for non-revoked token")
  public void shouldReturnFalseForNonRevokedToken() {
    String token = "non-revoked-refresh-token";
    Instant expirationDate = Instant.now().plusSeconds(3600);
    UUID userId = UUID.randomUUID();

    RefreshTokenDomain refreshToken = RefreshTokenDomain.restore(UUID.randomUUID(), token, expirationDate, false,
        userId);

    assertFalse(refreshToken.isRevoked());
    assertTrue(refreshToken.isValid());
  }

  @Test
  @DisplayName("Should return false for expired and revoked token")
  public void shouldReturnFalseForExpiredAndRevokedToken() {
    String token = "expired-and-revoked-refresh-token";
    Instant expirationDate = Instant.now().minusSeconds(3600);
    UUID userId = UUID.randomUUID();

    RefreshTokenDomain refreshToken = RefreshTokenDomain.restore(UUID.randomUUID(), token, expirationDate, true,
        userId);

    assertTrue(refreshToken.isExpired());
    assertTrue(refreshToken.isRevoked());
    assertFalse(refreshToken.isValid());
  }
}