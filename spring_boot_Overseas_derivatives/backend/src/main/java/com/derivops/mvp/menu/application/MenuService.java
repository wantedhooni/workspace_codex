package com.derivops.mvp.menu.application;

import com.derivops.mvp.common.SecurityUtils;
import com.derivops.mvp.menu.MenuEntry;
import com.derivops.mvp.menu.dto.MenuResponse;
import com.derivops.mvp.menu.infrastructure.MenuEntryRepository;
import com.derivops.mvp.user.UserRole;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MenuService {

    private static final String ROOT_KEY = "__root__";

    private final MenuEntryRepository menuEntryRepository;

    public List<MenuResponse> listMyMenus() {
        UserRole currentRole = SecurityUtils.currentRole().orElse(UserRole.OPS_VIEWER);
        List<MenuEntry> visibleMenus = menuEntryRepository.findByEnabledTrueOrderBySortOrderAscIdAsc()
                .stream()
                .filter(menu -> allowsRole(menu, currentRole))
                .toList();
        return buildTree(visibleMenus);
    }

    public Page<MenuResponse> list(String keyword, String filter, Pageable pageable) {
        Map<String, MenuEntry> allMenusByKey = menuEntryRepository.findAllByOrderBySortOrderAscIdAsc()
                .stream()
                .collect(LinkedHashMap::new, (map, menu) -> map.put(menu.getMenuKey(), menu), Map::putAll);

        return menuEntryRepository.search(keyword, filter, pageable)
                .map(menu -> toResponse(menu, resolveDepth(menu, allMenusByKey), List.of()));
    }

    private List<MenuResponse> buildTree(List<MenuEntry> menus) {
        Map<String, List<MenuEntry>> childrenByParent = new HashMap<>();
        Set<String> visibleKeys = new HashSet<>();

        for (MenuEntry menu : menus) {
            visibleKeys.add(menu.getMenuKey());
            String parentKey = normalizeParentMenuKey(menu.getParentMenuKey());
            childrenByParent.computeIfAbsent(parentKey == null ? ROOT_KEY : parentKey, key -> new ArrayList<>())
                    .add(menu);
        }

        List<MenuResponse> roots = new ArrayList<>();
        for (MenuEntry menu : menus) {
            String parentKey = normalizeParentMenuKey(menu.getParentMenuKey());
            if (parentKey == null || !visibleKeys.contains(parentKey)) {
                roots.add(toTreeResponse(menu, 0, childrenByParent, new HashSet<>()));
            }
        }
        return roots;
    }

    private MenuResponse toTreeResponse(
            MenuEntry menu,
            int depth,
            Map<String, List<MenuEntry>> childrenByParent,
            Set<String> visitedKeys
    ) {
        if (!visitedKeys.add(menu.getMenuKey())) {
            return toResponse(menu, depth, List.of());
        }

        List<MenuResponse> children = childrenByParent.getOrDefault(menu.getMenuKey(), List.of())
                .stream()
                .map(child -> toTreeResponse(child, depth + 1, childrenByParent, new HashSet<>(visitedKeys)))
                .toList();

        return toResponse(menu, depth, children);
    }

    private MenuResponse toResponse(MenuEntry menu, int depth, List<MenuResponse> children) {
        return new MenuResponse(
                menu.getId(),
                menu.getMenuKey(),
                menu.getTitle(),
                menu.getDescription(),
                menu.getPath(),
                normalizeParentMenuKey(menu.getParentMenuKey()),
                depth,
                menu.getResourceName(),
                menu.getIcon(),
                menu.getSortOrder(),
                menu.isEnabled(),
                parseRoles(menu.getRolesCsv()),
                children
        );
    }

    private int resolveDepth(MenuEntry menu, Map<String, MenuEntry> allMenusByKey) {
        int depth = 0;
        String parentKey = normalizeParentMenuKey(menu.getParentMenuKey());
        Set<String> visitedKeys = new HashSet<>();
        while (parentKey != null && visitedKeys.add(parentKey)) {
            MenuEntry parent = allMenusByKey.get(parentKey);
            if (parent == null) {
                break;
            }
            depth++;
            parentKey = normalizeParentMenuKey(parent.getParentMenuKey());
        }
        return depth;
    }

    private boolean allowsRole(MenuEntry menu, UserRole role) {
        List<String> roles = parseRoles(menu.getRolesCsv());
        if (roles.isEmpty()) {
            return true;
        }
        return roles.contains(role.name());
    }

    private String normalizeParentMenuKey(String parentMenuKey) {
        if (parentMenuKey == null || parentMenuKey.isBlank()) {
            return null;
        }
        return parentMenuKey.trim();
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
