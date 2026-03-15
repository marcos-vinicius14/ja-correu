package org.jacorreu.shared.validation;


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

}
