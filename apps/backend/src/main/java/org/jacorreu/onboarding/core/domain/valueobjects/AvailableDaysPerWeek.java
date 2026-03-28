package org.jacorreu.onboarding.core.domain.valueobjects;

import org.jacorreu.shared.validation.Notification;
import org.jacorreu.shared.validation.Result;

public final class AvailableDaysPerWeek {

    private static final int MIN = 2;
    private static final int MAX = 7;

    private final int value;

    private AvailableDaysPerWeek(int value) {
        this.value = value;
    }

    public static Result<AvailableDaysPerWeek> create(int value) {
        Notification notification = new Notification();

        if (value < MIN || value > MAX) {
            notification.addError("availableDaysPerWeek",
                    "Dias disponíveis por semana deve ser entre " + MIN + " e " + MAX);
        }

        if (notification.hasErrors()) {
            return Result.failure(notification);
        }

        return Result.success(new AvailableDaysPerWeek(value));
    }

    public static AvailableDaysPerWeek restore(int value) {
        return new AvailableDaysPerWeek(value);
    }

    public int getValue() {
        return value;
    }
}
