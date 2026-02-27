package com.portal.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public final class AuthDtos {
    private AuthDtos() {}

    @Schema(name = "LoginRequest", description = "Login payload")
    public record LoginRequest(
            @Schema(description = "Username", example = "admin")
            @NotBlank String username,
            @Schema(description = "Password", example = "admin1234")
            @NotBlank String password
    ) {}

    @Schema(name = "LoginResponse", description = "Login response")
    public record LoginResponse(
            @Schema(description = "Access token", example = "demo-token")
            String token,
            @Schema(description = "User info")
            Map<String, Object> user
    ) {}
}
