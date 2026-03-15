package org.jacorreu.user.core.domain.valueobjects;

import org.jacorreu.shared.validation.Notification;
import org.jacorreu.shared.validation.Result;

import java.time.Instant;
import java.util.function.Consumer;

public record ValidStravaToken(
        String accessToken,
        long expiresAt,
        String refreshToken
) implements StravaToken {
    public static Result<ValidStravaToken> create(String token, long expiresAt, String refreshToken) {
        Notification notification = new Notification();

        if (token == null || token.isBlank()) {
            notification.addError("Strava", "Falha ao conectar com Strava. Token inválido.");
        }

        if (refreshToken == null || refreshToken.isBlank()) {
            notification.addError("Strava", "Falha ao conectar com Strava. Refresh token inválido.");
        }

        if (expiresAt <= 0) {
            notification.addError("Strava", "Falha ao conectar com Strava. Data de expiração inválida.");
        }

        if (notification.hasErrors()) {
            return Result.failure(notification);
        }

        return Result.success(new ValidStravaToken(token, expiresAt, refreshToken));
    }

    public static ValidStravaToken restore(String token, String refreshToken, long expiresAt) {
        return new ValidStravaToken(token, expiresAt, refreshToken);
    }

    @Override
    public boolean isLinked() {
        return true;
    }

    @Override
    public boolean isExpired() {
        long now = Instant.now().getEpochSecond();
        return now >= this.expiresAt;
    }

    @Override
    public void execute(Consumer<String> action) {
        action.accept(this.accessToken);
    }
}
