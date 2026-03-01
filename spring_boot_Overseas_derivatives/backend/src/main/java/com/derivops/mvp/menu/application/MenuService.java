package com.derivops.mvp.menu.application;
import com.derivops.mvp.menu.*;
import com.derivops.mvp.menu.api.*;
import com.derivops.mvp.menu.dto.*;
import com.derivops.mvp.menu.infrastructure.*;


import com.derivops.mvp.common.SecurityUtils;
import com.derivops.mvp.user.UserRole;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MenuService {

    private final MenuEntryRepository menuEntryRepository;

    public List<MenuResponse> listMyMenus() {
        UserRole currentRole = SecurityUtils.currentRole().orElse(UserRole.OPS_VIEWER);
        return menuEntryRepository.findByEnabledTrueOrderBySortOrderAsc()
                .stream()
                .filter(menu -> allowsRole(menu, currentRole))
                .map(this::toResponse)
                .toList();
    }

    public Page<MenuResponse> list(String keyword, String filter, Pageable pageable) {
        return menuEntryRepository.search(keyword, filter, pageable).map(this::toResponse);
    }

    private boolean allowsRole(MenuEntry menu, UserRole role) {
        List<String> roles = parseRoles(menu.getRolesCsv());
        if (roles.isEmpty()) {
            return true;
        }
        return roles.contains(role.name());
    }

    private MenuResponse toResponse(MenuEntry menu) {
        return new MenuResponse(
                menu.getId(),
                menu.getMenuKey(),
                menu.getTitle(),
                menu.getDescription(),
                menu.getPath(),
                menu.getResourceName(),
                menu.getIcon(),
                menu.getSortOrder(),
                menu.isEnabled(),
                parseRoles(menu.getRolesCsv())
        );
    }

    private List<String> parseRoles(String csv) {
        if (csv == null || csv.isBlank()) {
            return List.of();
        }
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(s -> s.toUpperCase(Locale.ROOT))
                .toList();
    }
}
