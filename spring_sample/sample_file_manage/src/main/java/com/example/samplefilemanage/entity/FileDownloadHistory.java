package com.example.samplefilemanage.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 파일 다운로드 요청의 상세 이력을 저장하는 엔티티이다.
 */
@Entity
@Table(name = "file_download_histories")
public class FileDownloadHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stored_file_id", nullable = false)
    private StoredFile storedFile;

    @Column(nullable = false, length = 100)
    private String downloadedBy;

    @Column(length = 500)
    private String reason;

    @Column(nullable = false, length = 100)
    private String clientIp;

    @Column(length = 500)
    private String userAgent;

    @Column(nullable = false)
    private LocalDateTime downloadedAt;

    protected FileDownloadHistory() {
    }

    public FileDownloadHistory(
        StoredFile storedFile,
        String downloadedBy,
        String reason,
        String clientIp,
        String userAgent
    ) {
        this.storedFile = storedFile;
        this.downloadedBy = downloadedBy;
        this.reason = reason;
        this.clientIp = clientIp;
        this.userAgent = userAgent;
        this.downloadedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public StoredFile getStoredFile() {
        return storedFile;
    }

    public String getDownloadedBy() {
        return downloadedBy;
    }

    public String getReason() {
        return reason;
    }

    public String getClientIp() {
        return clientIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public LocalDateTime getDownloadedAt() {
        return downloadedAt;
    }
}
