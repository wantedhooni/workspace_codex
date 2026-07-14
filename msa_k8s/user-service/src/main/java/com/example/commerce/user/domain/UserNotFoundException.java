package com.example.commerce.user.domain;

import java.util.UUID;

/**
 * 요청한 식별자의 회원이 존재하지 않을 때 발생하는 예외다.
 */
public class UserNotFoundException extends RuntimeException {

    /**
     * 조회에 실패한 회원 식별자를 포함하는 예외를 생성한다.
     *
     * @param id 회원 식별자
     */
    public UserNotFoundException(UUID id) {
        super("회원을 찾을 수 없습니다: " + id);
    }
}

