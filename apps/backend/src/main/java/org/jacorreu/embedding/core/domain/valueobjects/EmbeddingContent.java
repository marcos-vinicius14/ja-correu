package org.jacorreu.embedding.core.domain.valueobjects;

import org.jacorreu.shared.validation.Notification;
import org.jacorreu.shared.validation.Result;

public final class EmbeddingContent {

    private final String value;

    private EmbeddingContent(String value) {
        this.value = value;
    }

    public static Result<EmbeddingContent> create(String value) {
        var notification = new Notification();

        if (value == null || value.isBlank()) {
            notification.addError("content", "content não pode ser nulo ou vazio");
        }

        return notification.hasErrors()
                ? Result.failure(notification)
                : Result.success(new EmbeddingContent(value));
    }

    public static EmbeddingContent restore(String value) {
        assert value != null && !value.isBlank();
        return new EmbeddingContent(value);
    }

    public String getValue() {
        return value;
    }
}