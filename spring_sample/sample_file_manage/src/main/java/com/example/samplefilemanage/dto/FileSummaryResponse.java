package com.example.samplefilemanage.dto;

import java.time.LocalDateTime;

/**
 * 파일 목록 화면에 필요한 요약 정보를 담는 응답 객체이다.
 */
public record FileSummaryResponse(
    Long fileId,
    String originalFileName,
    String contentType,
    long fileSize,
    String uploadedBy,
    String status,
    long downloadCount,
    LocalDateTime uploadedAt,
    LocalDateTime lastDownloadedAt
) {
}
