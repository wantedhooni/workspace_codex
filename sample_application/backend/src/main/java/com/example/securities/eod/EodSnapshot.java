package com.example.securities.eod;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "eod_snapshot")
public class EodSnapshot {

    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private LocalDate businessDate;

    @Column(nullable = false)
    private Long accountCount;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal totalBalance;

    @Column(nullable = false)
    private String reconciliationStatus;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected EodSnapshot() {
    }

    public EodSnapshot(LocalDate businessDate, Long accountCount, BigDecimal totalBalance, String reconciliationStatus) {
        this.id = UUID.randomUUID().toString();
        this.businessDate = businessDate;
        this.accountCount = accountCount;
        this.totalBalance = totalBalance;
        this.reconciliationStatus = reconciliationStatus;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public String getId() {
        return id;
    }

    public LocalDate getBusinessDate() {
        return businessDate;
    }

    public Long getAccountCount() {
        return accountCount;
    }

    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    public String getReconciliationStatus() {
        return reconciliationStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
