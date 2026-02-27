package com.quant.mvp.pipeline.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public final class SavedViewDefaultPayload {

    private SavedViewDefaultPayload() {
    }

    public record Req(
            @NotBlank(message = "resourceKey is required")
            String resourceKey,
            @NotNull(message = "viewId is required")
            Long viewId
    ) {
    }

    public record Res(
            String resourceKey,
            Long viewId,
            String viewName,
            Boolean shared,
            String ownerEmail,
            Instant pinnedAt
    ) {
    }
}
