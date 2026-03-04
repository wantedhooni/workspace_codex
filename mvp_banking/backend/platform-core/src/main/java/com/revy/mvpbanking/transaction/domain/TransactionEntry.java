package com.revy.mvpbanking.transaction.domain;

import com.revy.mvpbanking.common.domain.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class TransactionEntry extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "transaction_number", nullable = false, unique = true, length = 50)
    private String transactionNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransactionStatus status;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private Instant occurredAt;

    protected TransactionEntry() {
    }

    public TransactionEntry(
            UUID accountId,
            String transactionNumber,
            TransactionType transactionType,
            TransactionStatus status,
            BigDecimal amount,
            String currency,
            Instant occurredAt
    ) {
        this(accountId, transactionNumber, transactionType, status, amount, currency, null, occurredAt);
    }

    public TransactionEntry(
            UUID accountId,
            String transactionNumber,
            TransactionType transactionType,
            TransactionStatus status,
            BigDecimal amount,
            String currency,
            String description,
            Instant occurredAt
    ) {
        this.accountId = accountId;
        this.transactionNumber = transactionNumber;
        this.transactionType = transactionType;
        this.status = status;
        this.amount = amount;
        this.currency = currency;
        this.description = description;
        this.occurredAt = occurredAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getDescription() {
        return description;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public void updateSettlement(BigDecimal amount, String description, Instant occurredAt) {
        this.amount = amount;
        this.description = description;
        this.occurredAt = occurredAt;
    }
}
