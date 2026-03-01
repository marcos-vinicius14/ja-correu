package org.jacorreu.user.gateway;

import org.jacorreu.user.core.domain.valueobjects.StravaToken;

import java.util.UUID;

public interface StravaGateway {
    /**
     * PRIMEIRA CONEXÃO:
     * Troca o código de autorização (recebido no callback do frontend)
     * pelo primeiro conjunto de chaves definitivas.
     * * @param authorizationCode O código temporário gerado pelo Strava após o usuário clicar em "Autorizar".
     * @return O Value Object StravaToken.
     */
    StravaToken exchangeAuthorizationCode(String authorizationCode);
    void downloadActivitiesForUser(UUID userId, String accessToken);
    StravaToken refreshCredentials(StravaToken expiredToken);
    void revokeAccess(String accessToken);
}
