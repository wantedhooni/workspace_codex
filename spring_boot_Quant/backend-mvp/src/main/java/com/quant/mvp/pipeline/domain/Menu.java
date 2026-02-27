package com.quant.mvp.pipeline.domain;

import java.time.Instant;

public record Menu(
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
