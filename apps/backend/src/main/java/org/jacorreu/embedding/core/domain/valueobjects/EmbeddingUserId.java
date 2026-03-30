package org.jacorreu.embedding.core.domain.valueobjects;

import org.jacorreu.shared.validation.Notification;
import org.jacorreu.shared.validation.Result;

import java.util.UUID;

public final class EmbeddingUserId {

    private final UUID value;

    private EmbeddingUserId(UUID value) {
        this.value = value;
    }

    public static Result<EmbeddingUserId> create(UUID value) {
        var notification = new Notification();

        if (value == null) {
            notification.addError("userId", "userId não pode ser nulo");
        }

        return notification.hasErrors()
                ? Result.failure(notification)
                : Result.success(new EmbeddingUserId(value));
    }

    public static EmbeddingUserId restore(UUID value) {
        assert value != null;
        return new EmbeddingUserId(value);
    }

    public UUID getValue() {
        return value;
    }
}