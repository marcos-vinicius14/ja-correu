package org.jacorreu.shared.validation;

import java.util.function.Consumer;
import java.util.function.Function;

public final class Result<T> {
    private final T data;
    private final Notification notification;

    private Result(T data, Notification notification) {
        this.data = data;
        this.notification = notification;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(data, null);
    }

    public static Result<Void> success() {
        return new Result<>(null, null);
    }

    public static <T> Result<T> failure(Notification notification) {
        return new Result<>(null, notification);
    }

    public boolean isSuccess() {
        return notification == null || !notification.hasErrors();
    }

    public T getData() {
        return data;
    }

    public Notification getNotification() {
        return notification;
    }

    public void onFailure(Consumer<Error> action) {
        if (isFailure() && notification != null) {
            notification.getErrors().forEach(action);
        }
    }

    private boolean isFailure() {
        return notification != null && notification.hasErrors();
    }

    public <R> Result<R> map(Function<T, R> mapper) {
        if (isSuccess()) {
            return Result.success(mapper.apply(data));
        }
        return Result.failure(notification);
    }

    public <R> Result<R> flatMap(Function<T, Result<R>> mapper) {
        if (isSuccess()) {
            return mapper.apply(data);
        }
        return Result.failure(notification);
    }
}
