package com.example.samplefilemanage.dto;

/**
 * 파일 다운로드 이력 적재에 필요한 요청 정보를 담는 명령 객체이다.
 */
public record FileDownloadCommand(
    String downloadedBy,
    String reason,
    String clientIp,
    String userAgent
) {
}
