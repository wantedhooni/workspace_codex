package com.example.commerce.account.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * 고객이 보유한 금융 계좌의 현재 잔액과 상태를 나타내는 도메인 모델이다.
 *
 * @param id 계좌 식별자
 * @param ownerId 계좌 소유 회원 식별자
 * @param accountNumber 외부 노출용 계좌번호
 * @param currency ISO 4217 통화 코드
 * @param balance 현재 잔액
 * @param status 계좌 상태
 * @param version 동시 변경 감지용 버전
 * @param createdAt 계좌 개설 시각
 * @param updatedAt 최종 변경 시각
 */
public record Account(
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
}
