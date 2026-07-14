package com.example.commerce.user.domain;

/**
 * 이미 사용 중인 이메일로 회원을 생성할 때 발생하는 예외다.
 */
public class DuplicateEmailException extends RuntimeException {

    /**
     * 중복된 이메일을 포함하는 예외를 생성한다.
     *
     * @param email 중복 이메일
     */
    public DuplicateEmailException(String email) {
        super("이미 등록된 이메일입니다: " + email);
    }
}

