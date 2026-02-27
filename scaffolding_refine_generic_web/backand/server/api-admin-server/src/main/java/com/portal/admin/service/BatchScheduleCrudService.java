package com.portal.admin.service;

import com.portal.admin.domain.BatchSchedule;
import static com.portal.admin.dto.BatchScheduleDtos.*;
import com.portal.admin.repo.BatchJobRepository;
import com.portal.admin.repo.BatchScheduleRepository;
import com.portal.admin.repo.ServiceAuditLogRepository;
import com.portal.admin.service.base.BaseCrudService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BatchScheduleCrudService extends BaseCrudService<BatchSchedule, Long, CreateBatchScheduleRequest, UpdateBatchScheduleRequest, BatchScheduleResponse> {

    private final BatchJobRepository jobs;

    public BatchScheduleCrudService(BatchScheduleRepository repository, BatchJobRepository jobs, ServiceAuditLogRepository auditLogs) {
        super(repository, auditLogs);
        this.jobs = jobs;
    }

    @Override
    protected BatchSchedule createEntity(CreateBatchScheduleRequest request) {
        return new BatchSchedule(
                requireBatchJob(request.batchJobId()),
                request.cronExpression(),
                request.timezone(),
                request.enabled() == null ? Boolean.TRUE : request.enabled(),
                request.lastStatus(),
                request.lastRunAt()
        );
    }

    @Override
    protected void applyUpdate(BatchSchedule entity, UpdateBatchScheduleRequest request) {
        if (request.batchJobId() != null) entity.setBatchJobId(requireBatchJob(request.batchJobId()));
        if (request.cronExpression() != null) entity.setCronExpression(request.cronExpression());
        if (request.timezone() != null) entity.setTimezone(request.timezone());
        if (request.enabled() != null) entity.setEnabled(request.enabled());
        if (request.lastStatus() != null) entity.setLastStatus(request.lastStatus());
        if (request.lastRunAt() != null) entity.setLastRunAt(request.lastRunAt());
    }

    @Override
    protected BatchScheduleResponse toResponse(BatchSchedule entity) {
        return new BatchScheduleResponse(
                entity.getId(),
                entity.getBatchJobId(),
                entity.getCronExpression(),
                entity.getTimezone(),
                entity.getEnabled(),
                entity.getLastStatus(),
                entity.getLastRunAt()
        );
    }

    private Long requireBatchJob(Long batchJobId) {
        if (!jobs.existsById(batchJobId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "batchJobId not found");
        }
        return batchJobId;
    }
}
