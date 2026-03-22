package org.jacorreu.shared.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Notification {
    private final List<Error> errors = new ArrayList<>();


    public void addError(String field, String message) {
        errors.add(Error.of(field, message));
    }

    public void addError(String message) {
        errors.add(Error.of(message));
    }

    public boolean hasErrors() {
        return !this.errors.isEmpty();
    }

    public List<Error> getErrors() {
        return List.copyOf(errors);
    }
}
