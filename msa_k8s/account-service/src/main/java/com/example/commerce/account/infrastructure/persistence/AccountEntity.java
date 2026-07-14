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
import jakarta.persistence.Version;

import com.example.commerce.account.domain.Account;
import com.example.commerce.account.domain.AccountStatus;
import com.example.commerce.account.domain.AccountUnavailableException;
import com.example.commerce.account.domain.InsufficientBalanceException;

/**
 * PostgreSQL의 bank_account 테이블과 계좌 도메인 모델을 연결하는 JPA 엔티티다.
 */
@Entity
@Table(name = "bank_account", schema = "account")
public class AccountEntity {

    @Id
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * JPA가 엔티티를 복원할 때 사용하는 생성자다.
     */
    protected AccountEntity() {
    }

    private AccountEntity(
            UUID id,
            UUID ownerId,
            String accountNumber,
            String currency,
            BigDecimal balance,
            AccountStatus status,
            long version,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.accountNumber = accountNumber;
        this.currency = currency;
        this.balance = balance;
        this.status = status;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 계좌 도메인 모델을 영속 엔티티로 변환한다.
     *
     * @param account 변환할 계좌
     * @return 계좌 영속 엔티티
     */
    public static AccountEntity from(Account account) {
        return new AccountEntity(
                account.id(),
                account.ownerId(),
                account.accountNumber(),
                account.currency(),
                account.balance(),
                account.status(),
                account.version(),
                account.createdAt(),
                account.updatedAt());
    }

    /**
     * 활성 계좌에 금액을 더한다.
     *
     * @param amount 입금 금액
     * @param changedAt 잔액 변경 시각
     */
    public void deposit(BigDecimal amount, Instant changedAt) {
        requireActive();
        balance = balance.add(amount);
        updatedAt = changedAt;
    }

    /**
     * 활성 계좌의 잔액을 확인한 뒤 금액을 차감한다.
     *
     * @param amount 출금 금액
     * @param changedAt 잔액 변경 시각
     */
    public void withdraw(BigDecimal amount, Instant changedAt) {
        requireActive();
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException(balance, amount);
        }
        balance = balance.subtract(amount);
        updatedAt = changedAt;
    }

    /**
     * 영속 엔티티를 계좌 도메인 모델로 변환한다.
     *
     * @return 계좌 도메인 모델
     */
    public Account toDomain() {
        return new Account(id, ownerId, accountNumber, currency, balance, status, version, createdAt, updatedAt);
    }

    /**
     * 현재 계좌 식별자를 반환한다.
     *
     * @return 계좌 식별자
     */
    public UUID getId() {
        return id;
    }

    /**
     * 현재 계좌 잔액을 반환한다.
     *
     * @return 계좌 잔액
     */
    public BigDecimal getBalance() {
        return balance;
    }

    private void requireActive() {
        if (status != AccountStatus.ACTIVE) {
            throw new AccountUnavailableException(status);
        }
    }
}
