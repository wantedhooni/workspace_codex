package com.example.commerce.account.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.example.commerce.account.domain.AccountTransaction;
import com.example.commerce.account.domain.TransactionType;

/**
 * PostgreSQL의 account_ledger_entry 테이블과 불변 거래 원장을 연결하는 JPA 엔티티다.
 */
@Entity
@Table(name = "account_ledger_entry", schema = "account")
public class AccountTransactionEntity {

    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false, updatable = false)
    private UUID accountId;

    @Column(name = "idempotency_key", nullable = false, updatable = false)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private TransactionType type;

    @Column(nullable = false, updatable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, updatable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Column(updatable = false)
    private String memo;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * JPA가 엔티티를 복원할 때 사용하는 생성자다.
     */
    protected AccountTransactionEntity() {
    }

    private AccountTransactionEntity(
            UUID id,
            UUID accountId,
            String idempotencyKey,
            TransactionType type,
            BigDecimal amount,
            BigDecimal balanceAfter,
            String memo,
            Instant createdAt) {
        this.id = id;
        this.accountId = accountId;
        this.idempotencyKey = idempotencyKey;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.memo = memo;
        this.createdAt = createdAt;
    }

    /**
     * 거래 원장 도메인 모델을 영속 엔티티로 변환한다.
     *
     * @param transaction 변환할 거래 원장
     * @return 거래 원장 영속 엔티티
     */
    public static AccountTransactionEntity from(AccountTransaction transaction) {
        return new AccountTransactionEntity(
                transaction.id(),
                transaction.accountId(),
                transaction.idempotencyKey(),
                transaction.type(),
                transaction.amount(),
                transaction.balanceAfter(),
                transaction.memo(),
                transaction.createdAt());
    }

    /**
     * 영속 엔티티를 거래 원장 도메인 모델로 변환한다.
     *
     * @return 거래 원장 도메인 모델
     */
    public AccountTransaction toDomain() {
        return new AccountTransaction(
                id, accountId, idempotencyKey, type, amount, balanceAfter, memo, createdAt);
    }
}
