package com.example.marketsignal.user;

/**
 * 현재 로그인 사용자의 요약 정보를 전달한다.
 */
public record CurrentUser(
        Long id,
        String email
) {
}
