package com.quant.mvp.pipeline.payload;

import java.time.Instant;
import java.util.List;

public final class MenuSearchPayload {

    private MenuSearchPayload() {
    }

    public record Req(
            Long menuId,
            Long parentMenuId,
            String menuKey,
            String menuLabel,
            Boolean enabled
    ) {
    }

    public record Item(
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

    public record Res(
            List<Item> items
    ) {
    }
}
