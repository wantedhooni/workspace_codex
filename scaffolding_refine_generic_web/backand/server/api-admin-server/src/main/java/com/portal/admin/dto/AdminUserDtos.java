package com.portal.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public final class AdminUserDtos {
    private AdminUserDtos() {}

    @Schema(name = "CreateAdminRequest", description = "Admin user creation payload")
    public record CreateAdminRequest(
            @Schema(description = "Login username", example = "admin")
            @NotBlank String username,
            @Schema(description = "Raw password (stored as BCrypt hash)", example = "admin1234")
            @NotBlank String passwordHash,
            @Schema(description = "Role IDs")
            List<Long> roleIds
    ) {}

    @Schema(name = "UpdateAdminRequest", description = "Admin user update payload")
    public record UpdateAdminRequest(
            @Schema(description = "Login username", example = "admin")
            String username,
            @Schema(description = "Raw password (stored as BCrypt hash)", example = "admin1234")
            String passwordHash,
            @Schema(description = "Role IDs")
            List<Long> roleIds
    ) {}

    @Schema(name = "AdminUserResponse", description = "Admin user response")
    public record AdminUserResponse(
            @Schema(description = "Admin ID", example = "1")
            Long id,
            @Schema(description = "Login username", example = "admin")
            String username,
            @Schema(description = "Role IDs")
            List<Long> roleIds
    ) {}
}
