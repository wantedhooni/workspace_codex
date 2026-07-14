package com.example.commerce.account.domain;

/**
 * 동일한 멱등 키를 다른 거래 내용으로 재사용할 때 발생하는 예외다.
 */
public class IdempotencyConflictException extends RuntimeException {

    /**
     * 충돌한 멱등 키를 포함해 예외를 생성한다.
     *
     * @param idempotencyKey 중복된 멱등 키
     */
    public IdempotencyConflictException(String idempotencyKey) {
        super("멱등 키가 다른 거래에 이미 사용되었습니다: " + idempotencyKey);
    }
}
