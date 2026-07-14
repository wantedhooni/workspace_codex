package com.example.commerce.account.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.example.commerce.account.domain.AccountTransaction;
import com.example.commerce.account.domain.TransactionType;

/**
 * 계좌 거래 원장 API의 응답 형식을 정의한다.
 *
 * @param id 거래 식별자
 * @param accountId 계좌 식별자
 * @param type 거래 유형
 * @param amount 거래 금액
 * @param balanceAfter 거래 직후 잔액
 * @param memo 거래 메모
 * @param createdAt 거래 처리 시각
 */
public record AccountTransactionResponse(
        UUID id,
        UUID accountId,
        TransactionType type,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String memo,
        Instant createdAt
) {

    /**
     * 거래 원장 도메인 모델을 외부 응답으로 변환하며 내부 멱등 키는 노출하지 않는다.
     *
     * @param transaction 변환할 거래 원장
     * @return 거래 원장 응답
     */
    public static AccountTransactionResponse from(AccountTransaction transaction) {
        return new AccountTransactionResponse(
                transaction.id(),
                transaction.accountId(),
                transaction.type(),
                transaction.amount(),
                transaction.balanceAfter(),
                transaction.memo(),
                transaction.createdAt());
    }
}
