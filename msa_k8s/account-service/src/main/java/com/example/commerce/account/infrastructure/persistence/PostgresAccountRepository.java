package com.example.commerce.account.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.example.commerce.account.domain.Account;
import com.example.commerce.account.domain.AccountNotFoundException;
import com.example.commerce.account.domain.AccountRepository;
import com.example.commerce.account.domain.AccountTransaction;
import com.example.commerce.account.domain.IdempotencyConflictException;
import com.example.commerce.account.domain.TransactionType;

/**
 * PostgreSQL을 계좌 원본 저장소와 불변 거래 원장으로 사용하는 저장소 구현체다.
 */
@Repository
public class PostgresAccountRepository implements AccountRepository {

    private final SpringDataAccountJpaRepository accountRepository;
    private final SpringDataAccountTransactionJpaRepository transactionRepository;

    /**
     * 계좌와 거래 원장 Spring Data 저장소를 주입받는다.
     *
     * @param accountRepository 계좌 JPA 저장소
     * @param transactionRepository 거래 원장 JPA 저장소
     */
    public PostgresAccountRepository(
            SpringDataAccountJpaRepository accountRepository,
            SpringDataAccountTransactionJpaRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * 신규 계좌를 PostgreSQL에 즉시 반영한다.
     *
     * @param account 저장할 계좌
     * @return 저장된 계좌
     */
    @Override
    public Account save(Account account) {
        return accountRepository.saveAndFlush(AccountEntity.from(account)).toDomain();
    }

    /**
     * 계좌 식별자로 현재 계좌 상태를 조회한다.
     *
     * @param id 계좌 식별자
     * @return 존재할 경우 계좌
     */
    @Override
    public Optional<Account> findById(UUID id) {
        return accountRepository.findById(id).map(AccountEntity::toDomain);
    }

    /**
     * 회원 식별자로 보유 계좌를 개설 순서로 조회한다.
     *
     * @param ownerId 회원 식별자
     * @return 회원 계좌 목록
     */
    @Override
    public List<Account> findByOwnerId(UUID ownerId) {
        return accountRepository.findAllByOwnerIdOrderByCreatedAtAscIdAsc(ownerId).stream()
                .map(AccountEntity::toDomain)
                .toList();
    }

    /**
     * 계좌 행 잠금 안에서 멱등 키를 확인하고 잔액과 거래 원장을 함께 반영한다.
     *
     * @param accountId 계좌 식별자
     * @param type 거래 유형
     * @param amount 거래 금액
     * @param memo 거래 메모
     * @param idempotencyKey 중복 방지 키
     * @param createdAt 처리 시각
     * @return 신규 또는 기존 원장 항목
     */
    @Override
    public AccountTransaction changeBalance(
            UUID accountId,
            TransactionType type,
            BigDecimal amount,
            String memo,
            String idempotencyKey,
            Instant createdAt) {
        AccountEntity account = accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        Optional<AccountTransactionEntity> existing =
                transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            AccountTransaction transaction = existing.get().toDomain();
            if (!isSameRequest(transaction, accountId, type, amount, memo)) {
                throw new IdempotencyConflictException(idempotencyKey);
            }
            return transaction;
        }

        if (type == TransactionType.DEPOSIT) {
            account.deposit(amount, createdAt);
        } else {
            account.withdraw(amount, createdAt);
        }
        accountRepository.saveAndFlush(account);

        AccountTransaction transaction = new AccountTransaction(
                UUID.randomUUID(),
                account.getId(),
                idempotencyKey,
                type,
                amount,
                account.getBalance(),
                memo,
                createdAt);
        try {
            return transactionRepository.saveAndFlush(AccountTransactionEntity.from(transaction)).toDomain();
        } catch (DataIntegrityViolationException exception) {
            throw new IdempotencyConflictException(idempotencyKey);
        }
    }

    /**
     * 계좌의 최근 원장을 최신순으로 제한 조회한다.
     *
     * @param accountId 계좌 식별자
     * @param limit 최대 조회 수
     * @return 최근 원장 목록
     */
    @Override
    public List<AccountTransaction> findRecentTransactions(UUID accountId, int limit) {
        return transactionRepository
                .findByAccountIdOrderByCreatedAtDescIdDesc(accountId, PageRequest.of(0, limit))
                .stream()
                .map(AccountTransactionEntity::toDomain)
                .toList();
    }

    private boolean isSameRequest(
            AccountTransaction transaction,
            UUID accountId,
            TransactionType type,
            BigDecimal amount,
            String memo) {
        return transaction.accountId().equals(accountId)
                && transaction.type() == type
                && transaction.amount().compareTo(amount) == 0
                && Objects.equals(transaction.memo(), memo);
    }
}
