package com.example.websample.global.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** 로그인 요청 값을 담는 공통 DTO입니다. */
public record LoginRequest(
        @Email @NotBlank String email,
        @NotBlank String password
) {
}
