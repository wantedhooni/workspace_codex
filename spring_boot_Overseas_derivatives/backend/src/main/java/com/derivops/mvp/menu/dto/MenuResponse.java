package com.derivops.mvp.menu.dto;
import com.derivops.mvp.menu.*;
import com.derivops.mvp.menu.api.*;
import com.derivops.mvp.menu.application.*;
import com.derivops.mvp.menu.infrastructure.*;


import java.util.List;

public record MenuResponse(
        Long id,
        String menuKey,
        String title,
        String description,
        String path,
        String resourceName,
        String icon,
        int sortOrder,
        boolean enabled,
        List<String> roles
) {
}
