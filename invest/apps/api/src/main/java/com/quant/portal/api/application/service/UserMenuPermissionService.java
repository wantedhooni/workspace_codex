package com.quant.portal.api.application.service;

import com.quant.portal.api.application.mapper.MenuPermissionMapper;
import com.quant.portal.api.application.mapper.UserMenuPermissionMapper;
import com.quant.portal.api.infrastructure.jpa.repository.MenuPermissionRepository;
import com.quant.portal.api.presentation.dto.userpermission.UserMenuPermissionResponse;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserMenuPermissionService {

    private static final List<String> DEFAULT_MENU_KEYS = List.of(
            "dashboard",
            "portfolios",
            "instruments",
            "transactions",
            "holdings",
            "users",
            "quant-strategies",
            "quant-signals",
            "macro-indicators",
            "menu-permissions"
    );

    private final MenuPermissionRepository menuPermissionRepository;

    public UserMenuPermissionService(MenuPermissionRepository menuPermissionRepository) {
        this.menuPermissionRepository = menuPermissionRepository;
    }

    @Transactional(readOnly = true)
    public List<UserMenuPermissionResponse> findByRoleCodes(Collection<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return List.of();
        }

        List<String> normalizedRoleCodes = roleCodes.stream()
                .filter(value -> value != null && !value.isBlank())
                .map(value -> value.toUpperCase(Locale.ROOT))
                .toList();

        Map<String, UserMenuPermissionResponse> merged = new LinkedHashMap<>();
        menuPermissionRepository.findByRoleCodeIn(normalizedRoleCodes)
                .stream()
                .map(MenuPermissionMapper::toUserDto)
                .forEach(permission -> merged.merge(
                        permission.menuKey(),
                        permission,
                        UserMenuPermissionMapper::merge
                ));

        if (normalizedRoleCodes.contains("ROLE_ADMIN")) {
            for (String menuKey : DEFAULT_MENU_KEYS) {
                merged.put(menuKey, new UserMenuPermissionResponse(menuKey, true, true, true, true));
            }
        }

        return merged.values().stream()
                .sorted((left, right) -> left.menuKey().compareTo(right.menuKey()))
                .toList();
    }
}
