package com.example.multitenancy.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 로그인에 필요한 테넌트와 계정 정보를 받는다.
 */
public record LoginRequest(
        @NotBlank(message = "tenantId는 필수입니다.")
        String tenantId,
        @NotBlank(message = "username은 필수입니다.")
        String username,
        @NotBlank(message = "password는 필수입니다.")
        String password
) {
}
