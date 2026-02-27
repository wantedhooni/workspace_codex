package com.quant.mvp.api;

import com.quant.mvp.pipeline.domain.MenuPermission;
import com.quant.mvp.pipeline.domain.PermissionAction;
import com.quant.mvp.pipeline.payload.DeleteMenuPermissionPayload;
import com.quant.mvp.pipeline.payload.MenuPermissionSearchPayload;
import com.quant.mvp.pipeline.payload.UpsertMenuPermissionPayload;
import com.quant.mvp.pipeline.service.AccessControlService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/menu-permissions")
public class MenuPermissionController {

    private final AccessControlService accessControlService;
    private final PermissionGuard permissionGuard;

    public MenuPermissionController(
            AccessControlService accessControlService,
            PermissionGuard permissionGuard
    ) {
        this.accessControlService = accessControlService;
        this.permissionGuard = permissionGuard;
    }

    @GetMapping
    public MenuPermissionSearchPayload.Res list(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) Long menuId,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) String menuKey
    ) {
        permissionGuard.require(userEmail, "menuPermissions", PermissionAction.READ);
        return new MenuPermissionSearchPayload.Res(
                accessControlService.searchMenuPermissions(roleId, menuId, roleCode, menuKey)
                        .stream()
                        .map(this::toItem)
                        .collect(Collectors.toList())
        );
    }

    @PutMapping
    public UpsertMenuPermissionPayload.Res upsert(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @Valid @RequestBody UpsertMenuPermissionPayload.Req req
    ) {
        permissionGuard.require(userEmail, "menuPermissions", PermissionAction.UPDATE);
        MenuPermission updated = accessControlService.upsertMenuPermission(
                req.menuId(),
                req.roleId(),
                req.canRead(),
                req.canCreate(),
                req.canUpdate(),
                req.canDelete()
        );
        return new UpsertMenuPermissionPayload.Res(
                updated.menuPermissionId(),
                updated.menuId(),
                updated.roleId(),
                updated.canRead(),
                updated.canCreate(),
                updated.canUpdate(),
                updated.canDelete(),
                updated.updatedAt()
        );
    }

    @DeleteMapping("/{menuPermissionId}")
    public DeleteMenuPermissionPayload.Res delete(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @PathVariable Long menuPermissionId
    ) {
        permissionGuard.require(userEmail, "menuPermissions", PermissionAction.DELETE);
        MenuPermission deleted = accessControlService.deleteMenuPermission(menuPermissionId);
        return new DeleteMenuPermissionPayload.Res(
                deleted.menuPermissionId(),
                deleted.menuId(),
                deleted.roleId(),
                Instant.now()
        );
    }

    private MenuPermissionSearchPayload.Item toItem(AccessControlService.MenuPermissionView view) {
        MenuPermission permission = view.permission();
        return new MenuPermissionSearchPayload.Item(
                permission.menuPermissionId(),
                permission.menuId(),
                view.menuKey(),
                permission.roleId(),
                view.roleCode(),
                permission.canRead(),
                permission.canCreate(),
                permission.canUpdate(),
                permission.canDelete(),
                permission.updatedAt()
        );
    }
}
