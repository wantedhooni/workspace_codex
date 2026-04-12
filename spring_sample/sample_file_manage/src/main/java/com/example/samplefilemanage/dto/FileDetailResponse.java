package com.example.samplefilemanage.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 파일 상세 화면에 필요한 메타데이터와 이력 정보를 담는 응답 객체이다.
 */
public record FileDetailResponse(
    Long fileId,
    String originalFileName,
    String storedFileName,
    String contentType,
    long fileSize,
    String uploadedBy,
    String description,
    String status,
    long downloadCount,
    LocalDateTime uploadedAt,
    LocalDateTime lastDownloadedAt,
    List<FileDownloadHistoryResponse> downloadHistories
) {
}
