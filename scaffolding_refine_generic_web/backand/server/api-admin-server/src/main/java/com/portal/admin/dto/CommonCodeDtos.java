package com.portal.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public final class CommonCodeDtos {
    private CommonCodeDtos() {}

    @Schema(name = "CreateCommonCodeRequest", description = "Common code creation payload")
    public record CreateCommonCodeRequest(
            @Schema(description = "Code group", example = "USER_STATUS")
            @NotBlank String groupCode,
            @Schema(description = "Code", example = "ACTIVE")
            @NotBlank String code,
            @Schema(description = "Display name", example = "Active")
            @NotBlank String name,
            @Schema(description = "Description", example = "Active user status")
            String description,
            @Schema(description = "Sort order", example = "10")
            Integer sortOrder,
            @Schema(description = "Enabled", example = "true")
            Boolean enabled
    ) {}

    @Schema(name = "UpdateCommonCodeRequest", description = "Common code update payload")
    public record UpdateCommonCodeRequest(
            @Schema(description = "Code group", example = "USER_STATUS")
            String groupCode,
            @Schema(description = "Code", example = "ACTIVE")
            String code,
            @Schema(description = "Display name", example = "Active")
            String name,
            @Schema(description = "Description", example = "Active user status")
            String description,
            @Schema(description = "Sort order", example = "10")
            Integer sortOrder,
            @Schema(description = "Enabled", example = "true")
            Boolean enabled
    ) {}

    @Schema(name = "CommonCodeResponse", description = "Common code response")
    public record CommonCodeResponse(
            @Schema(description = "ID", example = "1")
            Long id,
            @Schema(description = "Code group", example = "USER_STATUS")
            String groupCode,
            @Schema(description = "Code", example = "ACTIVE")
            String code,
            @Schema(description = "Display name", example = "Active")
            String name,
            @Schema(description = "Description")
            String description,
            @Schema(description = "Sort order")
            Integer sortOrder,
            @Schema(description = "Enabled")
            Boolean enabled
    ) {}
}
