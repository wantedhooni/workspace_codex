package com.portal.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public final class AccessLogDtos {
    private AccessLogDtos() {}

    @Schema(name = "CreateAccessLogRequest", description = "Access log creation payload")
    public record CreateAccessLogRequest(
            @Schema(description = "Username", example = "admin")
            @NotBlank String username,
            @Schema(description = "IP address", example = "127.0.0.1")
            @NotBlank String ipAddress,
            @Schema(description = "Action", example = "LOGIN")
            @NotBlank String action,
            @Schema(description = "Path", example = "/auth/login")
            @NotBlank String path,
            @Schema(description = "Success", example = "true")
            @NotNull Boolean success
    ) {}

    @Schema(name = "UpdateAccessLogRequest", description = "Access log update payload")
    public record UpdateAccessLogRequest(
            @Schema(description = "Username")
            String username,
            @Schema(description = "IP address")
            String ipAddress,
            @Schema(description = "Action")
            String action,
            @Schema(description = "Path")
            String path,
            @Schema(description = "Success")
            Boolean success
    ) {}

    @Schema(name = "AccessLogResponse", description = "Access log response")
    public record AccessLogResponse(
            @Schema(description = "ID")
            Long id,
            @Schema(description = "Username")
            String username,
            @Schema(description = "IP address")
            String ipAddress,
            @Schema(description = "Action")
            String action,
            @Schema(description = "Path")
            String path,
            @Schema(description = "Success")
            Boolean success,
            @Schema(description = "Logged at")
            Instant loggedAt
    ) {}
}
