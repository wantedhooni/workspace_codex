package com.example.commerce.account.domain;

/**
 * 금융 계좌의 사용 가능 상태를 나타낸다.
 */
public enum AccountStatus {
    /** 정상 거래가 가능한 활성 계좌다. */
    ACTIVE,
    /** 운영 또는 규제 사유로 거래가 일시 중지된 계좌다. */
    SUSPENDED,
    /** 해지되어 더 이상 거래할 수 없는 계좌다. */
    CLOSED
}
