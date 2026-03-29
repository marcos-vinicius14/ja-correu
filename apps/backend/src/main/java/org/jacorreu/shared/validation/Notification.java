package org.jacorreu.shared.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Notification {
    private final List<Error> errors = new ArrayList<>();

    public Notification addError(String field, String message) {
        errors.add(Error.of(field, message));
        return this;
    }

    public Notification addError(String message) {
        errors.add(Error.of(message));
        return this;
    }

    public <T> Notification merge(Result<T> result) {
        if (result != null && !result.isSuccess()) {
            result.getNotification().getErrors().forEach(e -> addError(e.field(), e.message()));
        }
        return this;
    }

    public boolean hasErrors() {
        return !this.errors.isEmpty();
    }

    public List<Error> getErrors() {
        return List.copyOf(errors);
    }
}
