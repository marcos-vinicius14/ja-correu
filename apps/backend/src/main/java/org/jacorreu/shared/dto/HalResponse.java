package org.jacorreu.shared.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import org.springframework.hateoas.RepresentationModel;

public class HalResponse<T> extends RepresentationModel<HalResponse<T>> {

    @JsonUnwrapped
    private final ApiResponse<T> payload;

    private HalResponse(ApiResponse<T> payload) {
        this.payload = payload;
    }

    public static <T> HalResponse<T> of(ApiResponse<T> payload) {
        return new HalResponse<>(payload);
    }

    public ApiResponse<T> getPayload() {
        return payload;
    }
}
