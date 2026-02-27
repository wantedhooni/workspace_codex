package com.tradingmacro.menu;

import com.tradingmacro.user.User;
import jakarta.validation.constraints.NotNull;

public record MenuPermissionRequest(
    @NotNull User.Role role,
    @NotNull Long menuId,
    @NotNull Boolean canView,
    @NotNull Boolean canEdit
) {}
