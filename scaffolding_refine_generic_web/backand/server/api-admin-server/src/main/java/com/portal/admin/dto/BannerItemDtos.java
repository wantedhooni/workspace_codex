package com.portal.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public final class BannerItemDtos {
    private BannerItemDtos() {}

    @Schema(name = "CreateBannerItemRequest", description = "Banner creation payload")
    public record CreateBannerItemRequest(
            @Schema(description = "Title", example = "System Notice")
            @NotBlank String title,
            @Schema(description = "Image URL", example = "https://example.com/banner.png")
            @NotBlank String imageUrl,
            @Schema(description = "Link URL", example = "https://example.com")
            String linkUrl,
            @Schema(description = "Start at", example = "2026-02-01T00:00:00Z")
            String startAt,
            @Schema(description = "End at", example = "2026-12-31T23:59:59Z")
            String endAt,
            @Schema(description = "Enabled")
            Boolean enabled,
            @Schema(description = "Sort order", example = "1")
            Integer sortOrder
    ) {}

    @Schema(name = "UpdateBannerItemRequest", description = "Banner update payload")
    public record UpdateBannerItemRequest(
            @Schema(description = "Title")
            String title,
            @Schema(description = "Image URL")
            String imageUrl,
            @Schema(description = "Link URL")
            String linkUrl,
            @Schema(description = "Start at")
            String startAt,
            @Schema(description = "End at")
            String endAt,
            @Schema(description = "Enabled")
            Boolean enabled,
            @Schema(description = "Sort order")
            Integer sortOrder
    ) {}

    @Schema(name = "BannerItemResponse", description = "Banner response")
    public record BannerItemResponse(
            @Schema(description = "ID")
            Long id,
            @Schema(description = "Title")
            String title,
            @Schema(description = "Image URL")
            String imageUrl,
            @Schema(description = "Link URL")
            String linkUrl,
            @Schema(description = "Start at")
            String startAt,
            @Schema(description = "End at")
            String endAt,
            @Schema(description = "Enabled")
            Boolean enabled,
            @Schema(description = "Sort order")
            Integer sortOrder
    ) {}
}
