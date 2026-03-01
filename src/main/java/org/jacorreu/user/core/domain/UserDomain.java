package org.jacorreu.user.core.domain;

import com.fasterxml.uuid.Generators;
import org.jacorreu.user.core.domain.valueobjects.*;
import org.jacorreu.user.gateway.StravaGateway;

import java.util.Objects;
import java.util.UUID;

public final class UserDomain {
    private final UUID id;
    private final String name;
    private final Email email;
    private final Password password;
    private final StravaToken stravaToken;


    public UserDomain(String name, Email email, Password password) {
        this.id = Generators.timeBasedGenerator().generate();
        this.name = name;
        this.email = email;
        this.password = password;
        this.stravaToken = new EmptyStravaToken();
    }


    private UserDomain(UUID id, String name, Email email, Password password, StravaToken stravaToken) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.stravaToken = stravaToken;
    }


    public static UserDomain restore(
            UUID id,
            String name,
            String emailStr,
            String passwordStr,
            String stravaAccessToken,
            String stravaRefreshToken,
            Long stravaExpiresAt

    ) {
        Email restoredEmail = Email.restore(emailStr);
        Password restoredPassword = Password.restore(passwordStr);


        if (stravaAccessToken == null || stravaAccessToken.isBlank()) {
            return new UserDomain(id, name, restoredEmail, restoredPassword, new EmptyStravaToken());
        }

        StravaToken restoredToken = ValidStravaToken.restore(
                stravaAccessToken,
                stravaRefreshToken,
                stravaExpiresAt
        );

        return new UserDomain(id, name, restoredEmail, restoredPassword, restoredToken);
    }

    public UserDomain linkStrava(StravaToken token) {
        if (token == null || !token.isLinked()) {
            throw new IllegalArgumentException("Token inválido para vincular Strava.");
        }

        return new UserDomain(this.id, this.name, this.email, this.password, token);
    }

    public UserDomain unlinkStrava(StravaGateway stravaGateway) {
        this.stravaToken.execute(stravaGateway::revokeAccess);

        return new UserDomain(this.id, this.name, this.email, this.password, new EmptyStravaToken());
    }

    public UserDomain syncStravaActivities(StravaGateway stravaGateway) {

        final UserDomain updatedUser = this.stravaToken.isExpired()
                ? this.linkStrava(stravaGateway.refreshCredentials(this.stravaToken))
                : this;

        updatedUser.stravaToken.execute(accessToken -> {
            stravaGateway.downloadActivitiesForUser(updatedUser.id, accessToken);
        });

        return updatedUser;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Email getEmail() {
        return email;
    }

    public Password getPassword() {
        return password;
    }

    public StravaToken getStravaToken() {
        return stravaToken;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        UserDomain that = (UserDomain) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}


