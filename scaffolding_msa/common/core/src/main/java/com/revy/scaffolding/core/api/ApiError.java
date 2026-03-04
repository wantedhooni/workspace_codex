package com.revy.scaffolding.core.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
    String code,
    String message,
    LocalDateTime timestamp
) {
    public static ApiError of(String code, String message) {
        return new ApiError(code, message, LocalDateTime.now());
    }
}

