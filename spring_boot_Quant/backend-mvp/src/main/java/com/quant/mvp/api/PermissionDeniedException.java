package com.quant.mvp.api;

import com.quant.mvp.pipeline.domain.PermissionAction;

public class PermissionDeniedException extends RuntimeException {

    private final String menuKey;
    private final PermissionAction action;

    public PermissionDeniedException(String menuKey, PermissionAction action) {
        super("permission denied: menuKey=%s, action=%s".formatted(menuKey, action));
        this.menuKey = menuKey;
        this.action = action;
    }

    public String getMenuKey() {
        return menuKey;
    }

    public PermissionAction getAction() {
        return action;
    }
}
