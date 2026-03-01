package org.jacorreu.user.core.domain.valueobjects;

import org.jacorreu.shared.validation.Notification;
import org.jacorreu.shared.validation.Result;

public final class Email {
    private final String value;

    private Email(String value) {
        this.value = value;
    }

    public static Result<Email> create(String value) {
        Notification notification = new Notification();

        if (value == null || !value.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            notification.addError("email", "Formato de email inválido");
        }

        if (notification.hasErrors()) {
            return Result.failure(notification);
        }

        return Result.success(new Email(value));
    }

    public String getValue() {
        return value;
    }
}