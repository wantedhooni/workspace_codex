package com.example.samplefilemanage.dto;

import java.time.LocalDateTime;

/**
 * 파일 다운로드 이력 단건 정보를 담는 응답 객체이다.
 */
public record FileDownloadHistoryResponse(
    Long historyId,
    String downloadedBy,
    String reason,
    String clientIp,
    String userAgent,
    LocalDateTime downloadedAt
) {
}
