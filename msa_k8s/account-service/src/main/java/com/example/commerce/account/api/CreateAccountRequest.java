package com.example.commerce.account.api;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * 금융 계좌 개설 요청 형식을 정의한다.
 *
 * @param ownerId 계좌 소유 회원 식별자
 * @param currency ISO 4217 통화 코드
 */
public record CreateAccountRequest(
        @NotNull(message = "회원 식별자는 필수입니다.")
        UUID ownerId,

        @NotNull(message = "통화 코드는 필수입니다.")
        @Pattern(regexp = "^[A-Za-z]{3}$", message = "통화 코드는 영문 3자리여야 합니다.")
        String currency
) {
}
