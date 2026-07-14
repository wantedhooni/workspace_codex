package com.example.commerce.user.domain;

/**
 * 커머스 회원 계정의 현재 사용 상태를 나타낸다.
 */
public enum UserStatus {
    /**
     * 정상적으로 커머스 기능을 이용할 수 있는 계정이다.
     */
    ACTIVE,

    /**
     * 운영 정책에 따라 일시적으로 사용이 제한된 계정이다.
     */
    SUSPENDED
}
