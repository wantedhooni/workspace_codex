package com.quant.mvp.api;

import com.quant.mvp.pipeline.domain.Role;
import com.quant.mvp.pipeline.domain.PermissionAction;
import com.quant.mvp.pipeline.payload.CreateRolePayload;
import com.quant.mvp.pipeline.payload.DeleteRolePayload;
import com.quant.mvp.pipeline.payload.RoleSearchPayload;
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
@RequestMapping("/api/roles")
public class RoleController {

    private final AccessControlService accessControlService;
    private final PermissionGuard permissionGuard;

    public RoleController(AccessControlService accessControlService, PermissionGuard permissionGuard) {
        this.accessControlService = accessControlService;
        this.permissionGuard = permissionGuard;
    }

    @GetMapping
    public RoleSearchPayload.Res list(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) String roleCode,
            @RequestParam(required = false) String roleName
    ) {
        permissionGuard.require(userEmail, "roles", PermissionAction.READ);
        return new RoleSearchPayload.Res(
                accessControlService.searchRoles(roleId, roleCode, roleName).stream()
                        .map(this::toItem)
                        .collect(Collectors.toList())
        );
    }

    @PostMapping
    public CreateRolePayload.Res create(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @Valid @RequestBody CreateRolePayload.Req req
    ) {
        permissionGuard.require(userEmail, "roles", PermissionAction.CREATE);
        Role created = accessControlService.createRole(
                req.roleCode(),
                req.roleName(),
                req.description(),
                req.systemRole()
        );

        return new CreateRolePayload.Res(
                created.roleId(),
                created.roleCode(),
                created.roleName(),
                created.description(),
                created.systemRole(),
                created.createdAt()
        );
    }

    @DeleteMapping("/{roleId}")
    public DeleteRolePayload.Res delete(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @PathVariable Long roleId
    ) {
        permissionGuard.require(userEmail, "roles", PermissionAction.DELETE);
        Role deleted = accessControlService.deleteRole(roleId);
        return new DeleteRolePayload.Res(
                deleted.roleId(),
                deleted.roleCode(),
                Instant.now()
        );
    }

    private RoleSearchPayload.Item toItem(Role role) {
        return new RoleSearchPayload.Item(
                role.roleId(),
                role.roleCode(),
                role.roleName(),
                role.description(),
                role.systemRole(),
                role.createdAt()
        );
    }
}
