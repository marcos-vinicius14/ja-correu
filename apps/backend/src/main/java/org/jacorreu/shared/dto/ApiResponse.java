package org.jacorreu.shared.dto;

import java.util.List;

public record ApiResponse<T>(
        boolean success,
        T data,
        List<ErrorDto> errors
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, List.of());
    }

    public static <T> ApiResponse<T> error(String field, String message) {
        return new ApiResponse<>(false, null, List.of(new ErrorDto(field, message)));
    }

    public static <T> ApiResponse<T> error(List<ErrorDto> errors) {
        return new ApiResponse<>(false, null, errors);
    }

    public String getFirstErrorMessage() {
        return errors.isEmpty() ? "Unknown error" : errors.getFirst().message();
    }

    public record ErrorDto(String field, String message) {}
}
