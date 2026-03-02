package com.derivops.mvp.menu.dto;

import java.util.List;

public record MenuResponse(
        Long id,
        String menuKey,
        String title,
        String description,
        String path,
        String parentMenuKey,
        int depth,
        String resourceName,
        String icon,
        int sortOrder,
        boolean enabled,
        List<String> roles,
        List<MenuResponse> children
) {
}
