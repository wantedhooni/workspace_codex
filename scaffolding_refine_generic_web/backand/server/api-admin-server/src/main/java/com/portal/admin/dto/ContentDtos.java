package com.portal.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public final class ContentDtos {
    private ContentDtos() {}

    @Schema(name = "CreateContentRequest", description = "Content creation payload")
    public record CreateContentRequest(
            @Schema(description = "Title", example = "Welcome")
            @NotBlank String title,
            @Schema(description = "Slug", example = "welcome")
            @NotBlank String slug,
            @Schema(description = "Body", example = "Hello portal")
            @NotBlank String body,
            @Schema(description = "Status", example = "PUBLISHED")
            @NotBlank String status
    ) {}

    @Schema(name = "UpdateContentRequest", description = "Content update payload")
    public record UpdateContentRequest(
            @Schema(description = "Title", example = "Welcome")
            String title,
            @Schema(description = "Slug", example = "welcome")
            String slug,
            @Schema(description = "Body", example = "Hello portal")
            String body,
            @Schema(description = "Status", example = "PUBLISHED")
            String status
    ) {}

    @Schema(name = "ContentResponse", description = "Content response")
    public record ContentResponse(
            @Schema(description = "Content ID", example = "1")
            Long id,
            @Schema(description = "Title", example = "Welcome")
            String title,
            @Schema(description = "Slug", example = "welcome")
            String slug,
            @Schema(description = "Body")
            String body,
            @Schema(description = "Status", example = "PUBLISHED")
            String status,
            @Schema(description = "Updated timestamp")
            Instant updatedAt
    ) {}
}
