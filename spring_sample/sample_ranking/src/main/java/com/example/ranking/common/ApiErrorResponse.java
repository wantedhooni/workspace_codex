package com.example.ranking.common;

import java.time.LocalDateTime;
import java.util.List;

/**
 * API 오류 응답의 공통 형식이다.
 */
public record ApiErrorResponse(
        LocalDateTime timestamp,
        int status,
        String errorCode,
        String message,
        List<String> details
) {
}
