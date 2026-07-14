package com.example.commerce.user.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * 커머스 회원의 식별 정보와 계정 상태를 보관하는 도메인 모델이다.
 *
 * @param id 회원 식별자
 * @param email 정규화된 이메일 주소
 * @param name 회원 이름
 * @param status 계정 상태
 * @param createdAt 가입 시각
 */
public record User(
        UUID id,
        String email,
        String name,
        UserStatus status,
        Instant createdAt
) {
}

