package com.portal.admin.service;

import com.portal.admin.domain.ProgramItem;
import static com.portal.admin.dto.ProgramItemDtos.*;
import com.portal.admin.repo.ProgramItemRepository;
import com.portal.admin.repo.ServiceAuditLogRepository;
import com.portal.admin.service.base.BaseCrudService;
import org.springframework.stereotype.Service;

@Service
public class ProgramItemCrudService extends BaseCrudService<ProgramItem, Long, CreateProgramItemRequest, UpdateProgramItemRequest, ProgramItemResponse> {

    public ProgramItemCrudService(ProgramItemRepository repository, ServiceAuditLogRepository auditLogs) {
        super(repository, auditLogs);
    }

    @Override
    protected ProgramItem createEntity(CreateProgramItemRequest request) {
        return new ProgramItem(
                request.name(),
                request.url(),
                request.httpMethod(),
                request.description(),
                request.enabled() == null ? Boolean.TRUE : request.enabled()
        );
    }

    @Override
    protected void applyUpdate(ProgramItem entity, UpdateProgramItemRequest request) {
        if (request.name() != null) entity.setName(request.name());
        if (request.url() != null) entity.setUrl(request.url());
        if (request.httpMethod() != null) entity.setHttpMethod(request.httpMethod());
        if (request.description() != null) entity.setDescription(request.description());
        if (request.enabled() != null) entity.setEnabled(request.enabled());
    }

    @Override
    protected ProgramItemResponse toResponse(ProgramItem entity) {
        return new ProgramItemResponse(
                entity.getId(),
                entity.getName(),
                entity.getUrl(),
                entity.getHttpMethod(),
                entity.getDescription(),
                entity.getEnabled()
        );
    }
}
