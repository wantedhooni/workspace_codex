package com.example.commerce.account.domain;

import java.util.UUID;

/**
 * 요청한 계좌가 존재하지 않을 때 발생하는 예외다.
 */
public class AccountNotFoundException extends RuntimeException {

    /**
     * 조회에 실패한 계좌 식별자를 포함해 예외를 생성한다.
     *
     * @param accountId 계좌 식별자
     */
    public AccountNotFoundException(UUID accountId) {
        super("계좌를 찾을 수 없습니다: " + accountId);
    }
}
