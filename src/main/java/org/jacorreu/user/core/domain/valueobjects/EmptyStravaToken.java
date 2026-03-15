package org.jacorreu.user.core.domain.valueobjects;

import java.util.function.Consumer;

public record EmptyStravaToken() implements StravaToken {
    @Override
    public boolean isLinked() {
        return false;
    }

    @Override
    public boolean isExpired() {
        return false;
    }

    @Override
    public void execute(Consumer<String> action) {

    }
}
