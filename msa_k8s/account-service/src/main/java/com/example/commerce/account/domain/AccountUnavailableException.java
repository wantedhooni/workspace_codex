package com.example.commerce.account.domain;

/**
 * 활성 상태가 아닌 계좌에서 거래를 시도할 때 발생하는 예외다.
 */
public class AccountUnavailableException extends RuntimeException {

    /**
     * 현재 계좌 상태를 포함해 예외를 생성한다.
     *
     * @param status 현재 계좌 상태
     */
    public AccountUnavailableException(AccountStatus status) {
        super("현재 상태에서는 계좌 거래를 처리할 수 없습니다: " + status);
    }
}
