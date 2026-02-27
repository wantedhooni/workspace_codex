package com.portal.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public final class ServiceAuditLogDtos {
    private ServiceAuditLogDtos() {}

    @Schema(name = "CreateServiceAuditLogRequest", description = "Service audit log creation payload")
    public record CreateServiceAuditLogRequest(
            @Schema(description = "Domain type", example = "CONTENT")
            @NotBlank String domainType,
            @Schema(description = "Domain ID", example = "100")
            @NotNull Long domainId,
            @Schema(description = "Action", example = "UPDATE")
            @NotBlank String action,
            @Schema(description = "Username", example = "admin")
            @NotBlank String username,
            @Schema(description = "Detail", example = "Changed content title")
            String detail
    ) {}

    @Schema(name = "UpdateServiceAuditLogRequest", description = "Service audit log update payload")
    public record UpdateServiceAuditLogRequest(
            @Schema(description = "Domain type")
            String domainType,
            @Schema(description = "Domain ID")
            Long domainId,
            @Schema(description = "Action")
            String action,
            @Schema(description = "Username")
            String username,
            @Schema(description = "Detail")
            String detail
    ) {}

    @Schema(name = "ServiceAuditLogResponse", description = "Service audit log response")
    public record ServiceAuditLogResponse(
            @Schema(description = "ID")
            Long id,
            @Schema(description = "Domain type")
            String domainType,
            @Schema(description = "Domain ID")
            Long domainId,
            @Schema(description = "Action")
            String action,
            @Schema(description = "Username")
            String username,
            @Schema(description = "Detail")
            String detail,
            @Schema(description = "Logged at")
            Instant loggedAt
    ) {}
}
