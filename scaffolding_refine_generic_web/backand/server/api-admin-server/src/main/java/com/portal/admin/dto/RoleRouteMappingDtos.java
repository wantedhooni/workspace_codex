package com.portal.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class RoleRouteMappingDtos {
    private RoleRouteMappingDtos() {}

    @Schema(name = "CreateRoleRouteMappingRequest", description = "Role route mapping creation payload")
    public record CreateRoleRouteMappingRequest(
            @Schema(description = "Role ID", example = "1")
            @NotNull Long roleId,
            @Schema(description = "URL pattern", example = "/admin/**")
            @NotBlank String pattern,
            @Schema(description = "HTTP method", example = "GET")
            @NotBlank String httpMethod,
            @Schema(description = "Description")
            String description,
            @Schema(description = "Enabled")
            Boolean enabled
    ) {}

    @Schema(name = "UpdateRoleRouteMappingRequest", description = "Role route mapping update payload")
    public record UpdateRoleRouteMappingRequest(
            @Schema(description = "Role ID", example = "1")
            Long roleId,
            @Schema(description = "URL pattern", example = "/admin/**")
            String pattern,
            @Schema(description = "HTTP method", example = "GET")
            String httpMethod,
            @Schema(description = "Description")
            String description,
            @Schema(description = "Enabled")
            Boolean enabled
    ) {}

    @Schema(name = "RoleRouteMappingResponse", description = "Role route mapping response")
    public record RoleRouteMappingResponse(
            @Schema(description = "ID")
            Long id,
            @Schema(description = "Role ID")
            Long roleId,
            @Schema(description = "URL pattern")
            String pattern,
            @Schema(description = "HTTP method")
            String httpMethod,
            @Schema(description = "Description")
            String description,
            @Schema(description = "Enabled")
            Boolean enabled
    ) {}
}
