package com.example.commerce.user.api;

import java.time.Instant;
import java.util.UUID;

import com.example.commerce.user.domain.User;
import com.example.commerce.user.domain.UserStatus;

/**
 * 외부 API에 노출할 회원 응답 형식을 정의한다.
 *
 * @param id 회원 식별자
 * @param email 회원 이메일
 * @param name 회원 이름
 * @param status 계정 상태
 * @param createdAt 가입 시각
 */
public record UserResponse(
        UUID id,
        String email,
        String name,
        UserStatus status,
        Instant createdAt
) {

    /**
     * 도메인 회원 모델을 API 응답으로 변환한다.
     *
     * @param user 변환할 회원
     * @return 회원 API 응답
     */
    public static UserResponse from(User user) {
        return new UserResponse(
                user.id(),
                user.email(),
                user.name(),
                user.status(),
                user.createdAt());
    }
}

