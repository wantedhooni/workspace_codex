package com.example.marketsignal.common;

import java.time.LocalDateTime;

/**
 * API 예외 응답 본문을 정의한다.
 */
public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path
) {
}
