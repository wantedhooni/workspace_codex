package com.portal.admin.service;

import com.portal.admin.domain.RoleRouteMapping;
import static com.portal.admin.dto.RoleRouteMappingDtos.*;
import com.portal.admin.repo.RoleRepository;
import com.portal.admin.repo.RoleRouteMappingRepository;
import com.portal.admin.repo.ServiceAuditLogRepository;
import com.portal.admin.service.base.BaseCrudService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RoleRouteMappingCrudService extends BaseCrudService<RoleRouteMapping, Long, CreateRoleRouteMappingRequest, UpdateRoleRouteMappingRequest, RoleRouteMappingResponse> {

    private final RoleRepository roles;

    public RoleRouteMappingCrudService(RoleRouteMappingRepository repository, RoleRepository roles, ServiceAuditLogRepository auditLogs) {
        super(repository, auditLogs);
        this.roles = roles;
    }

    @Override
    protected RoleRouteMapping createEntity(CreateRoleRouteMappingRequest request) {
        return new RoleRouteMapping(
                requireRole(request.roleId()),
                request.pattern(),
                request.httpMethod(),
                request.description(),
                request.enabled() == null ? Boolean.TRUE : request.enabled()
        );
    }

    @Override
    protected void applyUpdate(RoleRouteMapping entity, UpdateRoleRouteMappingRequest request) {
        if (request.roleId() != null) entity.setRoleId(requireRole(request.roleId()));
        if (request.pattern() != null) entity.setPattern(request.pattern());
        if (request.httpMethod() != null) entity.setHttpMethod(request.httpMethod());
        if (request.description() != null) entity.setDescription(request.description());
        if (request.enabled() != null) entity.setEnabled(request.enabled());
    }

    @Override
    protected RoleRouteMappingResponse toResponse(RoleRouteMapping entity) {
        return new RoleRouteMappingResponse(
                entity.getId(),
                entity.getRoleId(),
                entity.getPattern(),
                entity.getHttpMethod(),
                entity.getDescription(),
                entity.getEnabled()
        );
    }

    private Long requireRole(Long roleId) {
        if (!roles.existsById(roleId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "roleId not found");
        }
        return roleId;
    }
}
