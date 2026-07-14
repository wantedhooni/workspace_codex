package com.example.commerce.account.api;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 계좌 입출금 요청 금액과 메모를 정의한다.
 *
 * @param amount 0보다 큰 거래 금액
 * @param memo 선택 거래 메모
 */
public record BalanceChangeRequest(
        @NotNull(message = "거래 금액은 필수입니다.")
        @DecimalMin(value = "0.01", message = "거래 금액은 0.01 이상이어야 합니다.")
        @Digits(integer = 17, fraction = 2, message = "거래 금액은 정수 17자리, 소수 2자리 이하여야 합니다.")
        BigDecimal amount,

        @Size(max = 200, message = "거래 메모는 200자 이하여야 합니다.")
        String memo
) {
}
