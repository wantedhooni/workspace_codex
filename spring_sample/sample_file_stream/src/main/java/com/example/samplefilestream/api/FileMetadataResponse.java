package com.example.samplefilestream.api;

import com.example.samplefilestream.file.domain.FileMetadata;
import java.time.Instant;
import java.util.UUID;

public record FileMetadataResponse(
    UUID id,
    String originalFilename,
    String storedFilename,
    String storageType,
    String storagePath,
    long sizeBytes,
    String contentType,
    String checksumSha256,
    Instant createdAt
) {

    public static FileMetadataResponse from(FileMetadata metadata) {
        return new FileMetadataResponse(
            metadata.getId(),
            metadata.getOriginalFilename(),
            metadata.getStoredFilename(),
            metadata.getStorageType().name(),
            metadata.getStoragePath(),
            metadata.getSizeBytes(),
            metadata.getContentType(),
            metadata.getChecksumSha256(),
            metadata.getCreatedAt()
        );
    }
}
