package com.quant.mvp.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        Instant timestamp,
        String code,
        String message,
        Object details
) {

    public static ErrorResponse of(String code, String message) {
        return new ErrorResponse(Instant.now(), code, message, null);
    }

    public static ErrorResponse of(String code, String message, Object details) {
        return new ErrorResponse(Instant.now(), code, message, details);
    }
}
