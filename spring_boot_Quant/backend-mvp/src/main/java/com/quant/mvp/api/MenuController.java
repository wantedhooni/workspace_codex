package com.quant.mvp.api;

import com.quant.mvp.pipeline.domain.Menu;
import com.quant.mvp.pipeline.domain.PermissionAction;
import com.quant.mvp.pipeline.payload.CreateMenuPayload;
import com.quant.mvp.pipeline.payload.DeleteMenuPayload;
import com.quant.mvp.pipeline.payload.MenuSearchPayload;
import com.quant.mvp.pipeline.service.AccessControlService;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    private final AccessControlService accessControlService;
    private final PermissionGuard permissionGuard;

    public MenuController(AccessControlService accessControlService, PermissionGuard permissionGuard) {
        this.accessControlService = accessControlService;
        this.permissionGuard = permissionGuard;
    }

    @GetMapping
    public MenuSearchPayload.Res list(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @RequestParam(required = false) Long menuId,
            @RequestParam(required = false) Long parentMenuId,
            @RequestParam(required = false) String menuKey,
            @RequestParam(required = false) String menuLabel,
            @RequestParam(required = false) Boolean enabled
    ) {
        permissionGuard.require(userEmail, "menus", PermissionAction.READ);
        return new MenuSearchPayload.Res(
                accessControlService.searchMenus(menuId, parentMenuId, menuKey, menuLabel, enabled).stream()
                        .map(this::toItem)
                        .collect(Collectors.toList())
        );
    }

    @PostMapping
    public CreateMenuPayload.Res create(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @Valid @RequestBody CreateMenuPayload.Req req
    ) {
        permissionGuard.require(userEmail, "menus", PermissionAction.CREATE);
        Menu created = accessControlService.createMenu(
                req.parentMenuId(),
                req.menuKey(),
                req.menuLabel(),
                req.path(),
                req.icon(),
                req.sortOrder(),
                req.enabled()
        );

        return new CreateMenuPayload.Res(
                created.menuId(),
                created.parentMenuId(),
                created.menuKey(),
                created.menuLabel(),
                created.path(),
                created.icon(),
                created.sortOrder(),
                created.enabled(),
                created.createdAt(),
                created.updatedAt()
        );
    }

    @DeleteMapping("/{menuId}")
    public DeleteMenuPayload.Res delete(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @PathVariable Long menuId
    ) {
        permissionGuard.require(userEmail, "menus", PermissionAction.DELETE);
        Menu deleted = accessControlService.deleteMenu(menuId);
        return new DeleteMenuPayload.Res(
                deleted.menuId(),
                deleted.menuKey(),
                Instant.now()
        );
    }

    private MenuSearchPayload.Item toItem(Menu menu) {
        return new MenuSearchPayload.Item(
                menu.menuId(),
                menu.parentMenuId(),
                menu.menuKey(),
                menu.menuLabel(),
                menu.path(),
                menu.icon(),
                menu.sortOrder(),
                menu.enabled(),
                menu.createdAt(),
                menu.updatedAt()
        );
    }
}
