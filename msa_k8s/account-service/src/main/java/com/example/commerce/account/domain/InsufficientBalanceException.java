package com.example.commerce.account.domain;

import java.math.BigDecimal;

/**
 * 출금 요청 금액이 현재 잔액보다 클 때 발생하는 예외다.
 */
public class InsufficientBalanceException extends RuntimeException {

    /**
     * 잔액과 요청 금액을 포함해 예외를 생성한다.
     *
     * @param balance 현재 잔액
     * @param requestedAmount 출금 요청 금액
     */
    public InsufficientBalanceException(BigDecimal balance, BigDecimal requestedAmount) {
        super("출금 가능 잔액이 부족합니다. 잔액=%s, 요청금액=%s".formatted(balance, requestedAmount));
    }
}
