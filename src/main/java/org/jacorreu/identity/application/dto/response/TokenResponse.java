package org.jacorreu.identity.application.dto.response;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
}
