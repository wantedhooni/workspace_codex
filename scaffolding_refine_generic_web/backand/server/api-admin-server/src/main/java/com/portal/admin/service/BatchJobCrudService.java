package com.portal.admin.service;

import com.portal.admin.domain.BatchJob;
import static com.portal.admin.dto.BatchJobDtos.*;
import com.portal.admin.repo.BatchJobRepository;
import com.portal.admin.repo.ServiceAuditLogRepository;
import com.portal.admin.service.base.BaseCrudService;
import org.springframework.stereotype.Service;

@Service
public class BatchJobCrudService extends BaseCrudService<BatchJob, Long, CreateBatchJobRequest, UpdateBatchJobRequest, BatchJobResponse> {

    public BatchJobCrudService(BatchJobRepository repository, ServiceAuditLogRepository auditLogs) {
        super(repository, auditLogs);
    }

    @Override
    protected BatchJob createEntity(CreateBatchJobRequest request) {
        return new BatchJob(
                request.name(),
                request.jobKey(),
                request.description(),
                request.enabled() == null ? Boolean.TRUE : request.enabled()
        );
    }

    @Override
    protected void applyUpdate(BatchJob entity, UpdateBatchJobRequest request) {
        if (request.name() != null) entity.setName(request.name());
        if (request.jobKey() != null) entity.setJobKey(request.jobKey());
        if (request.description() != null) entity.setDescription(request.description());
        if (request.enabled() != null) entity.setEnabled(request.enabled());
    }

    @Override
    protected BatchJobResponse toResponse(BatchJob entity) {
        return new BatchJobResponse(
                entity.getId(),
                entity.getName(),
                entity.getJobKey(),
                entity.getDescription(),
                entity.getEnabled()
        );
    }
}
