package com.example.orderapp.common.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "idempotency_record")
public class IdempotencyRecord extends BaseEntity {

    @Column(nullable = false, unique = true, length = 128)
    private String idempotencyKey;

    @Column(nullable = false, length = 128)
    private String requestHash;

    @Column(nullable = false, length = 64)
    private String resourceType;

    @Column(nullable = false)
    private Long resourceId;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String responseSnapshot;

    @Column(nullable = false)
    private Instant expiresAt;

    protected IdempotencyRecord() {
    }

    public IdempotencyRecord(
        String idempotencyKey,
        String requestHash,
        String resourceType,
        Long resourceId,
        String status,
        String responseSnapshot,
        Instant expiresAt
    ) {
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.status = status;
        this.responseSnapshot = responseSnapshot;
        this.expiresAt = expiresAt;
    }
}
