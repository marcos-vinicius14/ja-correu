package org.jacorreu.identity.infra.web.dto;

import org.springframework.hateoas.RepresentationModel;

public class AuthTokenResponse extends RepresentationModel<AuthTokenResponse> {

    private final String accessToken;

    public AuthTokenResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
