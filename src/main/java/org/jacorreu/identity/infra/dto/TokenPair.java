package org.jacorreu.identity.infra.dto;

public record TokenPair(
        String accessToken,
        String refreshToken
) {
}
