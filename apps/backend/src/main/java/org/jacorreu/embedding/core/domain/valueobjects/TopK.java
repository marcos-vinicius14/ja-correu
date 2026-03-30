package org.jacorreu.embedding.core.domain.valueobjects;

import org.jacorreu.shared.validation.Notification;
import org.jacorreu.shared.validation.Result;

public final class TopK {

    private static final int MIN = 1;
    private static final int MAX = 100;

    private final int value;

    private TopK(int value) {
        this.value = value;
    }

    public static Result<TopK> create(int value) {
        var notification = new Notification();

        if (value < MIN || value > MAX) {
            notification.addError("topK", "topK deve ser entre " + MIN + " e " + MAX);
        }

        return notification.hasErrors()
                ? Result.failure(notification)
                : Result.success(new TopK(value));
    }

    public static TopK restore(int value) {
        return new TopK(value);
    }

    public int getValue() {
        return value;
    }
}