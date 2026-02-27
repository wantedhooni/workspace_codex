package com.portal.admin.menu.service;

import com.portal.admin.menu.domain.MenuItem;
import static com.portal.admin.menu.dto.MenuDtos.*;
import com.portal.admin.menu.repo.MenuRepository;
import com.portal.admin.repo.ServiceAuditLogRepository;
import com.portal.admin.service.base.BaseCrudService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class MenuCrudService extends BaseCrudService<MenuItem, Long, CreateMenuRequest, UpdateMenuRequest, MenuResponse> {

    private final MenuRepository menus;

    public MenuCrudService(MenuRepository menus, ServiceAuditLogRepository auditLogs) {
        super(menus, auditLogs);
        this.menus = menus;
    }

    public List<TreeMenuResponse> tree(String searchParam, Map<String, String> requestParams) {
        List<MenuItem> allMenus;
        Map<String, String> fieldFilters = requestParams.entrySet().stream()
                .filter(entry -> !isReservedQueryParam(entry.getKey()))
                .filter(entry -> entry.getValue() != null && !entry.getValue().isBlank())
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().trim()));
        validateFilterKeys(fieldFilters);

        if (!fieldFilters.isEmpty()) {
            allMenus = menus.searchByFields(fieldFilters);
        } else if (searchParam == null || searchParam.isBlank()) {
            allMenus = menus.findAllByOrderBySortOrderAscIdAsc();
        } else {
            allMenus = menus.search(searchParam.trim());
        }

        Map<Long, TreeNodeBuilder> nodeMap = new LinkedHashMap<>();
        List<TreeNodeBuilder> roots = new ArrayList<>();

        for (MenuItem menu : allMenus) {
            nodeMap.put(menu.getId(), new TreeNodeBuilder(menu.getId(), menu.getTitle(), menu.getPath(), menu.getParentId(), menu.getSortOrder()));
        }

        for (TreeNodeBuilder node : nodeMap.values()) {
            if (node.parentId == null) {
                roots.add(node);
                continue;
            }
            TreeNodeBuilder parent = nodeMap.get(node.parentId);
            if (parent == null) {
                roots.add(node);
                continue;
            }
            parent.children.add(node);
        }

        return roots.stream().map(TreeNodeBuilder::toResponse).toList();
    }

    @Override
    protected MenuItem createEntity(CreateMenuRequest request) {
        validateParent(request.parentId());
        return new MenuItem(request.title(), request.path(), request.parentId(), request.sortOrder());
    }

    @Override
    protected void applyUpdate(MenuItem item, UpdateMenuRequest request) {
        if (request.title() != null) item.setTitle(request.title());
        if (request.path() != null) item.setPath(request.path());
        if (request.parentId() != null) {
            validateParent(request.parentId());
            if (request.parentId().equals(item.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "menu cannot be its own parent");
            }
            item.setParentId(request.parentId());
        }
        if (request.sortOrder() != null) item.setSortOrder(request.sortOrder());
    }

    @Override
    protected MenuResponse toResponse(MenuItem item) {
        return new MenuResponse(item.getId(), item.getTitle(), item.getPath(), item.getParentId(), item.getSortOrder());
    }

    private boolean isReservedQueryParam(String key) {
        return switch (key) {
            case "page", "perPage", "sort", "order", "ids", "searchParam" -> true;
            default -> false;
        };
    }

    private void validateFilterKeys(Map<String, String> filters) {
        if (filters.isEmpty()) {
            return;
        }
        Set<String> allowed = menus.allowedFilterFields();
        List<String> unsupported = filters.keySet().stream()
                .filter(field -> !allowed.contains(field))
                .sorted()
                .toList();
        if (!unsupported.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported filter fields: " + String.join(", ", unsupported)
            );
        }
    }

    private void validateParent(Long parentId) {
        if (parentId == null) {
            return;
        }
        if (!menus.existsById(parentId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "parent menu not found: " + parentId);
        }
    }

    private static class TreeNodeBuilder {
        private final Long id;
        private final String title;
        private final String path;
        private final Long parentId;
        private final Integer sortOrder;
        private final List<TreeNodeBuilder> children = new ArrayList<>();

        private TreeNodeBuilder(Long id, String title, String path, Long parentId, Integer sortOrder) {
            this.id = id;
            this.title = title;
            this.path = path;
            this.parentId = parentId;
            this.sortOrder = sortOrder;
        }

        private TreeMenuResponse toResponse() {
            return new TreeMenuResponse(
                    id,
                    title,
                    path,
                    parentId,
                    sortOrder,
                    children.stream().map(TreeNodeBuilder::toResponse).toList()
            );
        }
    }
}
