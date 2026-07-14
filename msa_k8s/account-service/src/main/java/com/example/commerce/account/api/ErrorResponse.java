package com.example.commerce.account.api;

import java.time.Instant;
import java.util.Map;

/**
 * Account Service API 오류의 공통 응답 형식을 정의한다.
 *
 * @param timestamp 오류 발생 시각
 * @param status HTTP 상태 코드
 * @param code 애플리케이션 오류 코드
 * @param message 오류 메시지
 * @param path 요청 경로
 * @param correlationId 요청 상관관계 ID
 * @param fieldErrors 필드별 검증 오류
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        String correlationId,
        Map<String, String> fieldErrors
) {
}
