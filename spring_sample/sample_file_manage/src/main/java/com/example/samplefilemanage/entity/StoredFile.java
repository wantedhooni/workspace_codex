package com.example.samplefilemanage.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 업로드된 파일의 메타데이터와 다운로드 집계 정보를 저장하는 엔티티이다.
 */
@Entity
@Table(name = "stored_files")
public class StoredFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String originalFileName;

    @Column(nullable = false, length = 255, unique = true)
    private String storedFileName;

    @Column(nullable = false, length = 500)
    private String storagePath;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private long fileSize;

    @Column(nullable = false, length = 100)
    private String uploadedBy;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FileStatus status;

    @Column(nullable = false)
    private long downloadCount;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    private LocalDateTime lastDownloadedAt;

    protected StoredFile() {
    }

    public StoredFile(
        String originalFileName,
        String storedFileName,
        String storagePath,
        String contentType,
        long fileSize,
        String uploadedBy,
        String description
    ) {
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.storagePath = storagePath;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.uploadedBy = uploadedBy;
        this.description = description;
        this.status = FileStatus.ACTIVE;
        this.downloadCount = 0L;
        this.uploadedAt = LocalDateTime.now();
    }

    public void markDownloaded() {
        this.downloadCount += 1;
        this.lastDownloadedAt = LocalDateTime.now();
    }

    public void markDeleted() {
        this.status = FileStatus.DELETED;
    }

    public Long getId() {
        return id;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getStoredFileName() {
        return storedFileName;
    }

    public String getStoragePath() {
        return storagePath;
    }

    public String getContentType() {
        return contentType;
    }

    public long getFileSize() {
        return fileSize;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public String getDescription() {
        return description;
    }

    public FileStatus getStatus() {
        return status;
    }

    public long getDownloadCount() {
        return downloadCount;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public LocalDateTime getLastDownloadedAt() {
        return lastDownloadedAt;
    }
}
