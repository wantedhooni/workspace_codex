package com.example.samplefilemanage.service;

import com.example.samplefilemanage.entity.StoredFile;
import org.springframework.core.io.Resource;

/**
 * 다운로드 응답 생성을 위해 파일 메타데이터와 리소스를 함께 전달한다.
 */
public record FileDownloadResource(StoredFile storedFile, Resource resource) {
}
