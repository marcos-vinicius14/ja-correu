package org.jacorreu.user.core.domain;

import com.fasterxml.uuid.Generators;
import org.jacorreu.user.core.domain.valueobjects.*;
import org.jacorreu.user.core.gateway.StravaGateway;

import java.util.Objects;
import java.util.UUID;

public final class UserDomain {
    private final UUID id;
    private final String name;
    private final Email email;
    private final Password password;
    private final StravaToken stravaToken;
    private final UserStatus status;


    private UserDomain(String name, Email email, Password password) {
        this.id = Generators.timeBasedEpochGenerator().generate();
        this.name = name;
        this.email = email;
        this.password = password;
        this.stravaToken = new EmptyStravaToken();
        this.status = UserStatus.PENDING_ONBOARDING;
    }


    private UserDomain(UUID id, String name, Email email, Password password, StravaToken stravaToken, UserStatus status) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.stravaToken = stravaToken;
        this.status = status;
    }


    public static UserDomain create(
            String username,
            Email email,
            Password password
    ) {
        return new UserDomain(username, email, password);
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
        return restore(id, name, emailStr, passwordStr, stravaAccessToken, stravaRefreshToken, stravaExpiresAt,
                UserStatus.PENDING_ONBOARDING);
    }

    public static UserDomain restore(
            UUID id,
            String name,
            String emailStr,
            String passwordStr,
            String stravaAccessToken,
            String stravaRefreshToken,
            Long stravaExpiresAt,
            UserStatus status
    ) {
        Email restoredEmail = Email.restore(emailStr);
        Password restoredPassword = Password.restore(passwordStr);


        if (stravaAccessToken == null || stravaAccessToken.isBlank()) {
            return new UserDomain(id, name, restoredEmail, restoredPassword, new EmptyStravaToken(), status);
        }

        StravaToken restoredToken = ValidStravaToken.restore(
                stravaAccessToken,
                stravaRefreshToken,
                stravaExpiresAt
        );

        return new UserDomain(id, name, restoredEmail, restoredPassword, restoredToken, status);
    }

    public UserDomain activate() {
        return new UserDomain(this.id, this.name, this.email, this.password, this.stravaToken, UserStatus.ACTIVE);
    }

    public UserDomain linkStrava(StravaToken token) {
        if (token == null || !token.isLinked()) {
            throw new IllegalArgumentException("Token inválido para vincular Strava.");
        }

        return new UserDomain(this.id, this.name, this.email, this.password, token, this.status);
    }

    public UserDomain unlinkStrava(StravaGateway stravaGateway) {
        this.stravaToken.execute(stravaGateway::revokeAccess);

        return new UserDomain(this.id, this.name, this.email, this.password, new EmptyStravaToken(), this.status);
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

    public UserStatus getStatus() {
        return status;
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


