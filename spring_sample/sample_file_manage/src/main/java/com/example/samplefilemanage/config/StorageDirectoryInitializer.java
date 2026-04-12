package com.example.samplefilemanage.config;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 시작 시 로컬 파일 저장 디렉터리를 준비한다.
 */
@Component
public class StorageDirectoryInitializer {

    private final FileStorageProperties properties;

    public StorageDirectoryInitializer(FileStorageProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    void initialize() throws IOException {
        Files.createDirectories(Path.of(properties.storagePath()).toAbsolutePath().normalize());
    }
}
