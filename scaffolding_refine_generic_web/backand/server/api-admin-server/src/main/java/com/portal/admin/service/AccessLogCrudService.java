package com.portal.admin.service;

import com.portal.admin.domain.AccessLog;
import static com.portal.admin.dto.AccessLogDtos.*;
import com.portal.admin.repo.AccessLogRepository;
import com.portal.admin.repo.ServiceAuditLogRepository;
import com.portal.admin.service.base.BaseCrudService;
import org.springframework.stereotype.Service;

@Service
public class AccessLogCrudService extends BaseCrudService<AccessLog, Long, CreateAccessLogRequest, UpdateAccessLogRequest, AccessLogResponse> {

    public AccessLogCrudService(AccessLogRepository repository, ServiceAuditLogRepository auditLogs) {
        super(repository, auditLogs);
    }

    @Override
    protected AccessLog createEntity(CreateAccessLogRequest request) {
        return new AccessLog(
                request.username(),
                request.ipAddress(),
                request.action(),
                request.path(),
                request.success()
        );
    }

    @Override
    protected void applyUpdate(AccessLog entity, UpdateAccessLogRequest request) {
        if (request.username() != null) entity.setUsername(request.username());
        if (request.ipAddress() != null) entity.setIpAddress(request.ipAddress());
        if (request.action() != null) entity.setAction(request.action());
        if (request.path() != null) entity.setPath(request.path());
        if (request.success() != null) entity.setSuccess(request.success());
    }

    @Override
    protected AccessLogResponse toResponse(AccessLog entity) {
        return new AccessLogResponse(
                entity.getId(),
                entity.getUsername(),
                entity.getIpAddress(),
                entity.getAction(),
                entity.getPath(),
                entity.getSuccess(),
                entity.getLoggedAt()
        );
    }

    @Override
    protected boolean shouldAudit(String action) {
        return false;
    }
}
