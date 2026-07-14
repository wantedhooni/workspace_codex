package com.example.commerce.account.domain;

/**
 * 계좌 원장에 기록되는 잔액 변경 유형을 나타낸다.
 */
public enum TransactionType {
    /** 계좌 잔액을 증가시키는 입금이다. */
    DEPOSIT,
    /** 계좌 잔액을 감소시키는 출금이다. */
    WITHDRAWAL
}
