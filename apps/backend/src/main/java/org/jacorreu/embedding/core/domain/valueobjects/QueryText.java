package org.jacorreu.embedding.core.domain.valueobjects;

import org.jacorreu.shared.validation.Notification;
import org.jacorreu.shared.validation.Result;

public final class QueryText {

    private final String value;

    private QueryText(String value) {
        this.value = value;
    }

    public static Result<QueryText> create(String value) {
        var notification = new Notification();

        if (value == null || value.isBlank()) {
            notification.addError("query", "query não pode ser nulo ou vazio");
        }

        return notification.hasErrors()
                ? Result.failure(notification)
                : Result.success(new QueryText(value));
    }

    public static QueryText restore(String value) {
        assert value != null && !value.isBlank();
        return new QueryText(value);
    }

    public String getValue() {
        return value;
    }
}