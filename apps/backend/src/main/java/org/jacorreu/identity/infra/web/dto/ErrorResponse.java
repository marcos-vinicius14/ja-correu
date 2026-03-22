package org.jacorreu.identity.infra.web.dto;

public record ErrorResponse(
        String message,
        String cause,
        String action
) {
}
