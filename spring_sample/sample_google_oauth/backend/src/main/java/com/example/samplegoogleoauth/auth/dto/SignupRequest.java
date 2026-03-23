package com.example.samplegoogleoauth.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
    @NotBlank(message = "표시 이름은 필수입니다.")
    @Size(max = 50, message = "표시 이름은 50자를 넘을 수 없습니다.")
    String displayName,

    @NotBlank(message = "소속 조직은 필수입니다.")
    @Size(max = 80, message = "소속 조직은 80자를 넘을 수 없습니다.")
    String organization,

    @NotBlank(message = "직무는 필수입니다.")
    @Size(max = 60, message = "직무는 60자를 넘을 수 없습니다.")
    String jobTitle,

    boolean marketingConsent
) {
}
