package com.example.samplefilestream.storage;

import com.example.samplefilestream.config.StorageProperties;
import com.example.samplefilestream.exception.FileStorageOperationException;
import com.example.samplefilestream.exception.StorageBackendUnavailableException;
import com.example.samplefilestream.file.domain.FileStorageType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;

@Component
public class LocalFileContentStorage implements FileContentStorage {

    private final Path basePath;

    public LocalFileContentStorage(StorageProperties storageProperties) {
        this.basePath = Path.of(storageProperties.getLocal().getBasePath()).toAbsolutePath().normalize();
    }

    @Override
    public FileStorageType type() {
        return FileStorageType.LOCAL;
    }

    @Override
    public void upload(Path stagedFile, String objectKey, String contentType) {
        Path target = normalizeKey(objectKey);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(stagedFile, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new FileStorageOperationException("로컬 파일 저장에 실패했습니다.", e);
        }
    }

    @Override
    public DownloadedContent download(String objectKey) {
        Path target = normalizeKey(objectKey);
        if (!Files.exists(target)) {
            throw new StorageBackendUnavailableException("로컬 저장소 파일을 찾을 수 없습니다. key=" + objectKey);
        }

        try {
            String detectedContentType = Files.probeContentType(target);
            return new DownloadedContent(
                new FileSystemResource(target),
                Files.size(target),
                detectedContentType
            );
        } catch (IOException e) {
            throw new FileStorageOperationException("로컬 파일 조회에 실패했습니다.", e);
        }
    }

    private Path normalizeKey(String objectKey) {
        Path normalized = basePath.resolve(objectKey).normalize();
        if (!normalized.startsWith(basePath)) {
            throw new FileStorageOperationException("잘못된 파일 경로입니다.");
        }
        return normalized;
    }
}
