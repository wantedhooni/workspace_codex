package com.example.commerce.account.application;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.commerce.account.domain.Account;
import com.example.commerce.account.domain.AccountNotFoundException;
import com.example.commerce.account.domain.AccountRepository;
import com.example.commerce.account.domain.AccountStatus;
import com.example.commerce.account.domain.AccountTransaction;
import com.example.commerce.account.domain.TransactionType;
import com.example.commerce.metrics.BusinessMetric;

/**
 * 계좌 개설, 조회, 멱등 입출금 유스케이스를 조정하는 애플리케이션 서비스다.
 */
@Service
public class AccountService {

    private static final BigDecimal ZERO_BALANCE = new BigDecimal("0.00");

    private final AccountRepository accountRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final Clock clock;

    /**
     * 계좌 저장소, 계좌번호 생성기, 시계를 주입받아 서비스를 생성한다.
     *
     * @param accountRepository 계좌 저장소
     * @param accountNumberGenerator 계좌번호 생성기
     * @param clock 거래 시각 생성용 시계
     */
    public AccountService(
            AccountRepository accountRepository,
            AccountNumberGenerator accountNumberGenerator,
            Clock clock) {
        this.accountRepository = accountRepository;
        this.accountNumberGenerator = accountNumberGenerator;
        this.clock = clock;
    }

    /**
     * 회원과 통화를 지정해 잔액이 0인 활성 계좌를 개설한다.
     *
     * @param ownerId 계좌 소유 회원 식별자
     * @param currency ISO 4217 통화 코드
     * @return 개설된 계좌
     */
    @Transactional
    @BusinessMetric("account.create")
    public Account createAccount(UUID ownerId, String currency) {
        Instant now = Instant.now(clock);
        Account account = new Account(
                UUID.randomUUID(),
                ownerId,
                accountNumberGenerator.generate(),
                currency.strip().toUpperCase(Locale.ROOT),
                ZERO_BALANCE,
                AccountStatus.ACTIVE,
                0L,
                now,
                now);
        return accountRepository.save(account);
    }

    /**
     * 식별자로 계좌를 조회하고 존재하지 않으면 도메인 예외를 발생시킨다.
     *
     * @param id 계좌 식별자
     * @return 조회된 계좌
     */
    @Transactional(readOnly = true)
    @BusinessMetric("account.get")
    public Account getAccount(UUID id) {
        return accountRepository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }

    /**
     * 회원이 보유한 계좌를 개설 순서로 조회한다.
     *
     * @param ownerId 회원 식별자
     * @return 회원 계좌 목록
     */
    @Transactional(readOnly = true)
    @BusinessMetric("account.list")
    public List<Account> getAccounts(UUID ownerId) {
        return accountRepository.findByOwnerId(ownerId);
    }

    /**
     * 멱등 키를 기준으로 입금 요청을 한 번만 처리하고 원장에 기록한다.
     *
     * @param accountId 계좌 식별자
     * @param amount 입금 금액
     * @param memo 거래 메모
     * @param idempotencyKey 중복 방지 키
     * @return 입금 원장 항목
     */
    @Transactional
    @BusinessMetric("account.deposit")
    public AccountTransaction deposit(
            UUID accountId,
            BigDecimal amount,
            String memo,
            String idempotencyKey) {
        return accountRepository.changeBalance(
                accountId,
                TransactionType.DEPOSIT,
                amount,
                normalizeMemo(memo),
                idempotencyKey.strip(),
                Instant.now(clock));
    }

    /**
     * 계좌 잔액을 검증한 뒤 멱등하게 출금하고 원장에 기록한다.
     *
     * @param accountId 계좌 식별자
     * @param amount 출금 금액
     * @param memo 거래 메모
     * @param idempotencyKey 중복 방지 키
     * @return 출금 원장 항목
     */
    @Transactional
    @BusinessMetric("account.withdraw")
    public AccountTransaction withdraw(
            UUID accountId,
            BigDecimal amount,
            String memo,
            String idempotencyKey) {
        return accountRepository.changeBalance(
                accountId,
                TransactionType.WITHDRAWAL,
                amount,
                normalizeMemo(memo),
                idempotencyKey.strip(),
                Instant.now(clock));
    }

    /**
     * 계좌의 최근 원장을 최신순으로 조회한다.
     *
     * @param accountId 계좌 식별자
     * @param limit 최대 조회 수
     * @return 최근 원장 목록
     */
    @Transactional(readOnly = true)
    @BusinessMetric("account.transactions.list")
    public List<AccountTransaction> getTransactions(UUID accountId, int limit) {
        getAccount(accountId);
        return accountRepository.findRecentTransactions(accountId, limit);
    }

    private String normalizeMemo(String memo) {
        return memo == null ? null : memo.strip();
    }
}
