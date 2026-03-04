package com.revy.scaffolding.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserCreateRequest(
    @Email(message = "유효한 이메일 형식이어야 합니다.")
    @NotBlank(message = "이메일은 필수입니다.")
    String email,
    @NotBlank(message = "이름은 필수입니다.")
    String name
) {
}

