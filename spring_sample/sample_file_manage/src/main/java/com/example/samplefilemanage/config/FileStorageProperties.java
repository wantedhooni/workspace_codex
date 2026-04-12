package com.example.samplefilemanage.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 파일 저장소 경로를 관리하는 설정 객체이다.
 */
@ConfigurationProperties(prefix = "app.file")
public record FileStorageProperties(String storagePath) {
}
