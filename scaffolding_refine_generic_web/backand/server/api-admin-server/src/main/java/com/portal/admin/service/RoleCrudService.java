package com.portal.admin.service;

import com.portal.admin.api.base.RelationResolver;
import com.portal.admin.domain.Permission;
import com.portal.admin.domain.Role;
import static com.portal.admin.dto.RoleDtos.*;
import com.portal.admin.repo.PermissionRepository;
import com.portal.admin.repo.RoleRepository;
import com.portal.admin.repo.ServiceAuditLogRepository;
import com.portal.admin.service.base.BaseCrudService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class RoleCrudService extends BaseCrudService<Role, Long, CreateRoleRequest, UpdateRoleRequest, RoleResponse> {

    private final PermissionRepository permissions;

    public RoleCrudService(RoleRepository roles, PermissionRepository permissions, ServiceAuditLogRepository auditLogs) {
        super(roles, auditLogs);
        this.permissions = permissions;
    }

    @Override
    protected Role createEntity(CreateRoleRequest request) {
        Role role = new Role(request.name(), request.description());
        role.setPermissions(resolvePermissions(request.permissionIds()));
        return role;
    }

    @Override
    protected void applyUpdate(Role role, UpdateRoleRequest request) {
        if (request.name() != null) role.setName(request.name());
        if (request.description() != null) role.setDescription(request.description());
        if (request.permissionIds() != null) role.setPermissions(resolvePermissions(request.permissionIds()));
    }

    @Override
    protected RoleResponse toResponse(Role role) {
        List<Long> permissionIds = role.getPermissions().stream().map(Permission::getId).sorted().toList();
        return new RoleResponse(role.getId(), role.getName(), role.getDescription(), permissionIds);
    }

    private Set<Permission> resolvePermissions(List<Long> permissionIds) {
        return RelationResolver.resolveRequired(permissions, permissionIds, "permissionIds");
    }
}
