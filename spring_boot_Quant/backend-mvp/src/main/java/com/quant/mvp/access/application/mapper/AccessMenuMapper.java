package com.quant.mvp.access.application.mapper;

import com.quant.mvp.access.domain.AccessMenu;
import com.quant.mvp.pipeline.domain.Menu;

public final class AccessMenuMapper {

    private AccessMenuMapper() {
        throw new UnsupportedOperationException("This class should never be instantiated");
    }

    public static Menu toDomain(AccessMenu menu) {
        if (menu == null) {
            return null;
        }
        return new Menu(
                menu.getId(),
                menu.getParentMenuId(),
                menu.getMenuKey(),
                menu.getMenuLabel(),
                menu.getPath(),
                menu.getIcon(),
                menu.getSortOrder(),
                menu.isEnabled(),
                menu.getCreatedAt(),
                menu.getUpdatedAt()
        );
    }
}
