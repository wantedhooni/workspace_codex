package com.revy.scaffolding.core.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
    boolean success,
    T data,
    ApiError error,
    LocalDateTime timestamp
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, LocalDateTime.now());
    }

    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, null, null, LocalDateTime.now());
    }

    public static ApiResponse<Void> error(String code, String message) {
        return new ApiResponse<>(false, null, ApiError.of(code, message), LocalDateTime.now());
    }
}
