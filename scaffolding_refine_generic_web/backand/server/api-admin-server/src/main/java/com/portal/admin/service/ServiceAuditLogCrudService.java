package com.portal.admin.service;

import com.portal.admin.domain.ServiceAuditLog;
import static com.portal.admin.dto.ServiceAuditLogDtos.*;
import com.portal.admin.repo.ServiceAuditLogRepository;
import com.portal.admin.service.base.BaseCrudService;
import org.springframework.stereotype.Service;

@Service
public class ServiceAuditLogCrudService extends BaseCrudService<ServiceAuditLog, Long, CreateServiceAuditLogRequest, UpdateServiceAuditLogRequest, ServiceAuditLogResponse> {

    public ServiceAuditLogCrudService(ServiceAuditLogRepository repository) {
        super(repository, repository);
    }

    @Override
    protected ServiceAuditLog createEntity(CreateServiceAuditLogRequest request) {
        return new ServiceAuditLog(
                request.domainType(),
                request.domainId(),
                request.action(),
                request.username(),
                request.detail()
        );
    }

    @Override
    protected void applyUpdate(ServiceAuditLog entity, UpdateServiceAuditLogRequest request) {
        if (request.domainType() != null) entity.setDomainType(request.domainType());
        if (request.domainId() != null) entity.setDomainId(request.domainId());
        if (request.action() != null) entity.setAction(request.action());
        if (request.username() != null) entity.setUsername(request.username());
        if (request.detail() != null) entity.setDetail(request.detail());
    }

    @Override
    protected ServiceAuditLogResponse toResponse(ServiceAuditLog entity) {
        return new ServiceAuditLogResponse(
                entity.getId(),
                entity.getDomainType(),
                entity.getDomainId(),
                entity.getAction(),
                entity.getUsername(),
                entity.getDetail(),
                entity.getLoggedAt()
        );
    }

    @Override
    protected boolean shouldAudit(String action) {
        return false;
    }
}
