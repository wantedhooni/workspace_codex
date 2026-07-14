package com.example.commerce.account.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.example.commerce.account.domain.Account;
import com.example.commerce.account.domain.AccountStatus;

/**
 * 계좌 API의 응답 형식을 정의한다.
 *
 * @param id 계좌 식별자
 * @param ownerId 계좌 소유 회원 식별자
 * @param accountNumber 계좌번호
 * @param currency 통화 코드
 * @param balance 현재 잔액
 * @param status 계좌 상태
 * @param version 계좌 변경 버전
 * @param createdAt 개설 시각
 * @param updatedAt 최종 변경 시각
 */
public record AccountResponse(
        UUID id,
        UUID ownerId,
        String accountNumber,
        String currency,
        BigDecimal balance,
        AccountStatus status,
        long version,
        Instant createdAt,
        Instant updatedAt
) {

    /**
     * 계좌 도메인 모델을 API 응답으로 변환한다.
     *
     * @param account 변환할 계좌
     * @return 계좌 응답
     */
    public static AccountResponse from(Account account) {
        return new AccountResponse(
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
}
