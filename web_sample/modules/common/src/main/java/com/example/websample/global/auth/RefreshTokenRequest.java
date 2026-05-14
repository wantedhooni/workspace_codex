package com.example.websample.global.auth;

import jakarta.validation.constraints.NotBlank;

/** 리프레시 토큰 재발급 요청 값을 담는 공통 DTO입니다. */
public record RefreshTokenRequest(
        @NotBlank String refreshToken
) {
}
