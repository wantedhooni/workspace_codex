package com.portal.admin.service;

import com.portal.admin.domain.Permission;
import static com.portal.admin.dto.PermissionDtos.*;
import com.portal.admin.repo.PermissionRepository;
import com.portal.admin.repo.ServiceAuditLogRepository;
import com.portal.admin.service.base.BaseCrudService;
import org.springframework.stereotype.Service;

@Service
public class PermissionCrudService extends BaseCrudService<Permission, Long, CreatePermissionRequest, UpdatePermissionRequest, PermissionResponse> {

    public PermissionCrudService(PermissionRepository permissions, ServiceAuditLogRepository auditLogs) {
        super(permissions, auditLogs);
    }

    @Override
    protected Permission createEntity(CreatePermissionRequest request) {
        return new Permission(request.code(), request.description());
    }

    @Override
    protected void applyUpdate(Permission permission, UpdatePermissionRequest request) {
        if (request.code() != null) permission.setCode(request.code());
        if (request.description() != null) permission.setDescription(request.description());
    }

    @Override
    protected PermissionResponse toResponse(Permission permission) {
        return new PermissionResponse(permission.getId(), permission.getCode(), permission.getDescription());
    }
}
