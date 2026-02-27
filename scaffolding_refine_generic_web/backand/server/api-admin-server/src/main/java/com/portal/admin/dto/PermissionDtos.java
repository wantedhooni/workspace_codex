package com.portal.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public final class PermissionDtos {
    private PermissionDtos() {}

    @Schema(name = "CreatePermissionRequest", description = "Permission creation payload")
    public record CreatePermissionRequest(
            @Schema(description = "Permission code", example = "CONTENT_MANAGE")
            @NotBlank String code,
            @Schema(description = "Permission description", example = "Manage portal contents")
            String description
    ) {}

    @Schema(name = "UpdatePermissionRequest", description = "Permission update payload")
    public record UpdatePermissionRequest(
            @Schema(description = "Permission code", example = "CONTENT_MANAGE")
            String code,
            @Schema(description = "Permission description", example = "Manage portal contents")
            String description
    ) {}

    @Schema(name = "PermissionResponse", description = "Permission response")
    public record PermissionResponse(
            @Schema(description = "Permission ID", example = "1")
            Long id,
            @Schema(description = "Permission code", example = "CONTENT_MANAGE")
            String code,
            @Schema(description = "Permission description", example = "Manage portal contents")
            String description
    ) {}
}
