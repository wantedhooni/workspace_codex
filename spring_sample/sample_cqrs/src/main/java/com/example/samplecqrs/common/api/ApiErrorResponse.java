package com.example.samplecqrs.common.api;

import java.time.Instant;

/**
 * 공통 API 오류 응답 형식이다.
 *
 * @param message 오류 메시지
 * @param timestamp 오류 발생 시각
 */
public record ApiErrorResponse(
        String message,
        Instant timestamp
) {
}
