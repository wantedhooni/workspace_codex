package com.example.samplefilemanage.dto;

import java.time.LocalDateTime;

/**
 * 파일 업로드 완료 후 반환하는 응답 객체이다.
 */
public record FileUploadResponse(
    Long fileId,
    String originalFileName,
    String contentType,
    long fileSize,
    String uploadedBy,
    String status,
    LocalDateTime uploadedAt
) {
}
