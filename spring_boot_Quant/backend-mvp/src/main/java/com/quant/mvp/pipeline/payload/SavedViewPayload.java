package com.quant.mvp.pipeline.payload;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class SavedViewPayload {

    private SavedViewPayload() {
    }

    public record Item(
            Long viewId,
            String resourceKey,
            String viewName,
            String description,
            Boolean shared,
            String ownerEmail,
            Map<String, String> filters,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record Req(
            @NotBlank(message = "resourceKey is required")
            String resourceKey,
            @NotBlank(message = "viewName is required")
            String viewName,
            String description,
            Boolean shared,
            Map<String, String> filters
    ) {
    }

    public record CreateRes(
            Long viewId,
            String resourceKey,
            String viewName,
            String description,
            Boolean shared,
            String ownerEmail,
            Map<String, String> filters,
            Instant createdAt,
            Instant updatedAt
    ) {
    }

    public record DeleteRes(
            Long viewId,
            String resourceKey,
            String viewName,
            Instant deletedAt
    ) {
    }

    public record Res(
            List<Item> items
    ) {
    }
}
