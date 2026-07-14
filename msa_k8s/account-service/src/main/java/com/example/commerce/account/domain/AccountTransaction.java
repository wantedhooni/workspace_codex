package com.example.commerce.account.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * 계좌 잔액 변경을 재구성할 수 있도록 기록하는 불변 원장 항목이다.
 *
 * @param id 거래 식별자
 * @param accountId 계좌 식별자
 * @param idempotencyKey 중복 거래 방지 키
 * @param type 거래 유형
 * @param amount 거래 금액
 * @param balanceAfter 거래 직후 잔액
 * @param memo 거래 메모
 * @param createdAt 거래 처리 시각
 */
public record AccountTransaction(
        UUID id,
        UUID accountId,
        String idempotencyKey,
        TransactionType type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String memo,
        Instant createdAt
) {
}
