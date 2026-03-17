package com.example.marketsignal.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 프로필 수정 요청 모델이다.
 */
public record UpdateProfileRequest(
        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 80, message = "이름은 80자 이하여야 합니다.")
        String name,
        @Size(max = 500, message = "소개는 500자 이하여야 합니다.")
        String bio
) {
}
