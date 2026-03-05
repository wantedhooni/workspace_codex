package com.example.samplefilestream.file.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "file_metadata")
public class FileMetadata {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, length = 255)
    private String originalFilename;

    @Column(nullable = false, length = 255)
    private String storedFilename;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FileStorageType storageType;

    @Column(nullable = false, length = 1024)
    private String storagePath;

    @Column(nullable = false)
    private long sizeBytes;

    @Column(length = 255)
    private String contentType;

    @Column(length = 64)
    private String checksumSha256;

    @Column(nullable = false)
    private Instant createdAt;

    protected FileMetadata() {
    }

    public FileMetadata(
        String originalFilename,
        String storedFilename,
        FileStorageType storageType,
        String storagePath,
        long sizeBytes,
        String contentType,
        String checksumSha256
    ) {
        this.originalFilename = originalFilename;
        this.storedFilename = storedFilename;
        this.storageType = storageType;
        this.storagePath = storagePath;
        this.sizeBytes = sizeBytes;
        this.contentType = contentType;
        this.checksumSha256 = checksumSha256;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getStoredFilename() {
        return storedFilename;
    }

    public FileStorageType getStorageType() {
        return storageType;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public String getContentType() {
        return contentType;
    }

    public String getChecksumSha256() {
        return checksumSha256;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
