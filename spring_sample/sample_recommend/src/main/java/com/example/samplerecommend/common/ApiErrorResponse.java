package com.example.samplerecommend.common;

import java.time.LocalDateTime;

/**
 * API 예외 응답 형식을 정의한다.
 */
public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message
) {
}

