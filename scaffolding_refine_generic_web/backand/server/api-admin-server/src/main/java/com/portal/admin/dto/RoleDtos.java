package com.portal.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public final class RoleDtos {
    private RoleDtos() {}

    @Schema(name = "CreateRoleRequest", description = "Role creation payload")
    public record CreateRoleRequest(
            @Schema(description = "Role name", example = "SUPER_ADMIN")
            @NotBlank String name,
            @Schema(description = "Role description", example = "Full access")
            String description,
            @Schema(description = "Permission IDs")
            List<Long> permissionIds
    ) {}

    @Schema(name = "UpdateRoleRequest", description = "Role update payload")
    public record UpdateRoleRequest(
            @Schema(description = "Role name", example = "SUPER_ADMIN")
            String name,
            @Schema(description = "Role description", example = "Full access")
            String description,
            @Schema(description = "Permission IDs")
            List<Long> permissionIds
    ) {}

    @Schema(name = "RoleResponse", description = "Role response")
    public record RoleResponse(
            @Schema(description = "Role ID", example = "1")
            Long id,
            @Schema(description = "Role name", example = "SUPER_ADMIN")
            String name,
            @Schema(description = "Role description", example = "Full access")
            String description,
            @Schema(description = "Permission IDs")
            List<Long> permissionIds
    ) {}
}
