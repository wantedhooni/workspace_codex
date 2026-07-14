package com.example.commerce.account.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 계좌 원본 데이터와 거래 원장을 저장하고 조회하는 도메인 저장소 계약이다.
 */
public interface AccountRepository {

    /**
     * 신규 계좌를 저장한다.
     *
     * @param account 저장할 계좌
     * @return 저장된 계좌
     */
    Account save(Account account);

    /**
     * 식별자로 계좌를 조회한다.
     *
     * @param id 계좌 식별자
     * @return 존재할 경우 계좌
     */
    Optional<Account> findById(UUID id);

    /**
     * 회원이 보유한 계좌를 개설 순서로 조회한다.
     *
     * @param ownerId 회원 식별자
     * @return 회원 계좌 목록
     */
    List<Account> findByOwnerId(UUID ownerId);

    /**
     * 계좌 행을 잠근 뒤 멱등하게 잔액을 변경하고 원장 항목을 기록한다.
     *
     * @param accountId 계좌 식별자
     * @param type 거래 유형
     * @param amount 거래 금액
     * @param memo 거래 메모
     * @param idempotencyKey 중복 방지 키
     * @param createdAt 처리 시각
     * @return 생성되었거나 기존에 처리된 원장 항목
     */
    AccountTransaction changeBalance(
            UUID accountId,
            TransactionType type,
            BigDecimal amount,
            String memo,
            String idempotencyKey,
            Instant createdAt);

    /**
     * 계좌의 최근 거래 원장을 조회한다.
     *
     * @param accountId 계좌 식별자
     * @param limit 최대 조회 수
     * @return 최신순 원장 목록
     */
    List<AccountTransaction> findRecentTransactions(UUID accountId, int limit);
}
