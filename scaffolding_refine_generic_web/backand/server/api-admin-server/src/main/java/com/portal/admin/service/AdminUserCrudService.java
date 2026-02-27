package com.portal.admin.service;

import com.portal.admin.api.base.RelationResolver;
import com.portal.admin.domain.AdminUser;
import com.portal.admin.domain.Role;
import static com.portal.admin.dto.AdminUserDtos.*;
import com.portal.admin.repo.AdminUserRepository;
import com.portal.admin.repo.RoleRepository;
import com.portal.admin.repo.ServiceAuditLogRepository;
import com.portal.admin.service.base.BaseCrudService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class AdminUserCrudService extends BaseCrudService<AdminUser, Long, CreateAdminRequest, UpdateAdminRequest, AdminUserResponse> {

    private final RoleRepository roles;
    private final PasswordEncoder passwordEncoder;

    public AdminUserCrudService(
            AdminUserRepository users,
            RoleRepository roles,
            ServiceAuditLogRepository auditLogs,
            PasswordEncoder passwordEncoder
    ) {
        super(users, auditLogs);
        this.roles = roles;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected AdminUser createEntity(CreateAdminRequest request) {
        AdminUser user = new AdminUser(request.username(), passwordEncoder.encode(request.passwordHash()));
        user.setRoles(resolveRoles(request.roleIds()));
        return user;
    }

    @Override
    protected void applyUpdate(AdminUser user, UpdateAdminRequest request) {
        if (request.username() != null) user.setUsername(request.username());
        if (request.passwordHash() != null) {
            user.setPasswordHash(passwordEncoder.encode(request.passwordHash()));
            user.incrementTokenVersion();
        }
        if (request.roleIds() != null) user.setRoles(resolveRoles(request.roleIds()));
    }

    @Override
    protected AdminUserResponse toResponse(AdminUser user) {
        List<Long> roleIds = user.getRoles().stream().map(Role::getId).sorted().toList();
        return new AdminUserResponse(user.getId(), user.getUsername(), roleIds);
    }

    private Set<Role> resolveRoles(List<Long> roleIds) {
        return RelationResolver.resolveRequired(roles, roleIds, "roleIds");
    }
}
