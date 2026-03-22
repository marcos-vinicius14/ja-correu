package org.jacorreu.identity.core.gateway;

import java.util.UUID;

public interface JwtGateway {
    String generateToken(UUID userId, String email, String username);
    UUID extractUserId(String token);
}
