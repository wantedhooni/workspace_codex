package com.quant.portal.api.application.service;

import com.quant.portal.api.application.exception.ApiException;
import com.quant.portal.api.application.exception.ErrorCode;
import com.quant.portal.api.application.mapper.MenuPermissionMapper;
import com.quant.portal.api.application.query.MenuPermissionSearchCondition;
import com.quant.portal.api.infrastructure.jpa.repository.MenuPermissionRepository;
import com.quant.portal.api.presentation.dto.menupermission.MenuPermissionCreateRequest;
import com.quant.portal.api.presentation.dto.menupermission.MenuPermissionUpdateRequest;
import com.quant.portal.domain.admin.entity.MenuPermission;
import java.util.Locale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MenuPermissionService {

    private final MenuPermissionRepository menuPermissionRepository;

    public MenuPermissionService(MenuPermissionRepository menuPermissionRepository) {
        this.menuPermissionRepository = menuPermissionRepository;
    }

    @Transactional
    public MenuPermission create(MenuPermissionCreateRequest request) {
        String roleCode = normalizeRoleCode(request.roleCode());
        String menuKey = normalizeMenuKey(request.menuKey());

        menuPermissionRepository.findByRoleCodeAndMenuKey(roleCode, menuKey)
                .ifPresent(permission -> {
                    throw new ApiException(HttpStatus.CONFLICT, ErrorCode.CONFLICT, "Menu permission already exists");
                });

        MenuPermission menuPermission = MenuPermissionMapper.toEntity(request);
        return menuPermissionRepository.save(menuPermission);
    }

    @Transactional(readOnly = true)
    public MenuPermission get(Long id) {
        return menuPermissionRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, "Menu permission not found: " + id));
    }

    @Transactional(readOnly = true)
    public Page<MenuPermission> search(String keyword, String roleCode, Pageable pageable) {
        MenuPermissionSearchCondition condition = new MenuPermissionSearchCondition(keyword, normalizeRoleCodeNullable(roleCode));
        return menuPermissionRepository.search(condition, pageable);
    }

    @Transactional
    public MenuPermission update(Long id, MenuPermissionUpdateRequest request) {
        MenuPermission menuPermission = get(id);

        String roleCode = normalizeRoleCode(request.roleCode());
        String menuKey = normalizeMenuKey(request.menuKey());

        menuPermissionRepository.findByRoleCodeAndMenuKey(roleCode, menuKey)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new ApiException(HttpStatus.CONFLICT, ErrorCode.CONFLICT, "Menu permission already exists");
                    }
                });

        menuPermission.update(
                roleCode,
                menuKey,
                request.canList(),
                request.canCreate(),
                request.canEdit(),
                request.canDelete()
        );

        return menuPermission;
    }

    @Transactional
    public void delete(Long id) {
        MenuPermission menuPermission = get(id);
        menuPermissionRepository.delete(menuPermission);
    }

    private static String normalizeRoleCode(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizeRoleCodeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return normalizeRoleCode(value);
    }

    private static String normalizeMenuKey(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
