package com.portal.admin.service;

import com.portal.admin.domain.CommonCode;
import static com.portal.admin.dto.CommonCodeDtos.*;
import com.portal.admin.repo.CommonCodeRepository;
import com.portal.admin.repo.ServiceAuditLogRepository;
import com.portal.admin.service.base.BaseCrudService;
import org.springframework.stereotype.Service;

@Service
public class CommonCodeCrudService extends BaseCrudService<CommonCode, Long, CreateCommonCodeRequest, UpdateCommonCodeRequest, CommonCodeResponse> {

    public CommonCodeCrudService(CommonCodeRepository repository, ServiceAuditLogRepository auditLogs) {
        super(repository, auditLogs);
    }

    @Override
    protected CommonCode createEntity(CreateCommonCodeRequest request) {
        return new CommonCode(
                request.groupCode(),
                request.code(),
                request.name(),
                request.description(),
                request.sortOrder() == null ? 0 : request.sortOrder(),
                request.enabled() == null ? Boolean.TRUE : request.enabled()
        );
    }

    @Override
    protected void applyUpdate(CommonCode entity, UpdateCommonCodeRequest request) {
        if (request.groupCode() != null) entity.setGroupCode(request.groupCode());
        if (request.code() != null) entity.setCode(request.code());
        if (request.name() != null) entity.setName(request.name());
        if (request.description() != null) entity.setDescription(request.description());
        if (request.sortOrder() != null) entity.setSortOrder(request.sortOrder());
        if (request.enabled() != null) entity.setEnabled(request.enabled());
    }

    @Override
    protected CommonCodeResponse toResponse(CommonCode entity) {
        return new CommonCodeResponse(
                entity.getId(),
                entity.getGroupCode(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.getSortOrder(),
                entity.getEnabled()
        );
    }
}
