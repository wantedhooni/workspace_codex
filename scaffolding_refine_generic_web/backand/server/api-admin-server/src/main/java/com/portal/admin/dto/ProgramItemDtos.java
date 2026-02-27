package com.portal.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public final class ProgramItemDtos {
    private ProgramItemDtos() {}

    @Schema(name = "CreateProgramItemRequest", description = "Program creation payload")
    public record CreateProgramItemRequest(
            @Schema(description = "Program name", example = "Admin Dashboard")
            @NotBlank String name,
            @Schema(description = "URL", example = "/admins")
            @NotBlank String url,
            @Schema(description = "HTTP method", example = "GET")
            @NotBlank String httpMethod,
            @Schema(description = "Description", example = "Admin list page")
            String description,
            @Schema(description = "Enabled", example = "true")
            Boolean enabled
    ) {}

    @Schema(name = "UpdateProgramItemRequest", description = "Program update payload")
    public record UpdateProgramItemRequest(
            @Schema(description = "Program name", example = "Admin Dashboard")
            String name,
            @Schema(description = "URL", example = "/admins")
            String url,
            @Schema(description = "HTTP method", example = "GET")
            String httpMethod,
            @Schema(description = "Description")
            String description,
            @Schema(description = "Enabled")
            Boolean enabled
    ) {}

    @Schema(name = "ProgramItemResponse", description = "Program response")
    public record ProgramItemResponse(
            @Schema(description = "ID", example = "1")
            Long id,
            @Schema(description = "Program name")
            String name,
            @Schema(description = "URL")
            String url,
            @Schema(description = "HTTP method")
            String httpMethod,
            @Schema(description = "Description")
            String description,
            @Schema(description = "Enabled")
            Boolean enabled
    ) {}
}
