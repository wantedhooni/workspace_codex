package com.quant.mvp.pipeline.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public final class CreateMenuPayload {

    private CreateMenuPayload() {
    }

    public record Req(
            Long parentMenuId,
            @NotBlank String menuKey,
            @NotBlank String menuLabel,
            @NotBlank String path,
            String icon,
            @NotNull Integer sortOrder,
            @NotNull Boolean enabled
    ) {
    }

    public record Res(
            Long menuId,
            Long parentMenuId,
            String menuKey,
            String menuLabel,
            String path,
            String icon,
            Integer sortOrder,
            boolean enabled,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}
