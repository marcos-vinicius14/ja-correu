package org.jacorreu.user.core.domain;

import com.fasterxml.uuid.Generators;
import org.jacorreu.user.core.domain.valueobjects.Email;
import org.jacorreu.user.core.domain.valueobjects.Password;

import java.util.UUID;

public final class UserDomain {
    private final UUID id;
    private final String name;
    private final Email email;
    private final Password password;
    private final String strava_token;

    public UserDomain(String name, Email email, Password password, String strava_token) {
        this.id = Generators.timeBasedGenerator().generate();
        this.name = name;
        this.email = email;
        this.password = password;
        this.strava_token = strava_token;
    }
}
