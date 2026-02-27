package com.portal.admin.menu.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public final class MenuDtos {
    private MenuDtos() {}

    @Schema(name = "CreateMenuRequest", description = "Menu creation payload")
    public record CreateMenuRequest(
            @Schema(description = "Menu title", example = "Dashboard")
            @NotBlank String title,
            @Schema(description = "Menu path", example = "/dashboard")
            @NotBlank String path,
            @Schema(description = "Parent menu ID", example = "1")
            Long parentId,
            @Schema(description = "Sort order", example = "10")
            Integer sortOrder
    ) {}

    @Schema(name = "UpdateMenuRequest", description = "Menu update payload")
    public record UpdateMenuRequest(
            @Schema(description = "Menu title", example = "Dashboard")
            String title,
            @Schema(description = "Menu path", example = "/dashboard")
            String path,
            @Schema(description = "Parent menu ID", example = "1")
            Long parentId,
            @Schema(description = "Sort order", example = "10")
            Integer sortOrder
    ) {}

    @Schema(name = "MenuResponse", description = "Menu response")
    public record MenuResponse(
            @Schema(description = "Menu ID", example = "1")
            Long id,
            @Schema(description = "Menu title", example = "Dashboard")
            String title,
            @Schema(description = "Menu path", example = "/dashboard")
            String path,
            @Schema(description = "Parent menu ID", example = "1")
            Long parentId,
            @Schema(description = "Sort order", example = "10")
            Integer sortOrder
    ) {}

    @Schema(name = "TreeMenuResponse", description = "Menu tree node")
    public record TreeMenuResponse(
            @Schema(description = "Menu ID", example = "1")
            Long id,
            @Schema(description = "Menu title", example = "System")
            String title,
            @Schema(description = "Menu path", example = "/system")
            String path,
            @Schema(description = "Parent menu ID", example = "null")
            Long parentId,
            @Schema(description = "Sort order", example = "70")
            Integer sortOrder,
            @Schema(description = "Child nodes")
            List<TreeMenuResponse> children
    ) {}
}
