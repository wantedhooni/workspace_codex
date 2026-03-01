package com.derivops.mvp.batch.application;
import com.derivops.mvp.batch.*;
import com.derivops.mvp.batch.api.*;
import com.derivops.mvp.batch.dto.*;
import com.derivops.mvp.batch.infrastructure.*;
import com.derivops.mvp.batch.config.*;
import com.derivops.mvp.batch.job.*;


import com.derivops.mvp.audit.application.AuditLogService;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class OpsBatchExecutionService {

    private final BatchRunRepository batchRunRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public void execute(String batchName, String triggerSource) {
        BatchRun run = new BatchRun();
        run.setBatchName(batchName);
        run.setStatus(BatchStatus.RUNNING);
        run.setStartedAt(OffsetDateTime.now());
        run.setRetryCount(0);
        run = batchRunRepository.save(run);

        try {
            simulateWork(batchName);
            run.setStatus(BatchStatus.SUCCESS);
            run.setFinishedAt(OffsetDateTime.now());
            batchRunRepository.save(run);
            auditLogService.log("system", "BATCH_SUCCESS", "BATCH_RUN", run.getId().toString(), triggerSource + ":" + batchName);
        } catch (Exception ex) {
            run.setStatus(BatchStatus.FAILED);
            run.setRetryCount(1);
            run.setErrorMessage(ex.getMessage());
            run.setFinishedAt(OffsetDateTime.now());
            batchRunRepository.save(run);
            auditLogService.log("system", "BATCH_FAILED", "BATCH_RUN", run.getId().toString(), triggerSource + ":" + ex.getMessage());
            throw ex;
        }
    }

    private void simulateWork(String batchName) {
        // deterministic failure windows for monitoring/demo
        int minute = OffsetDateTime.now().getMinute();
        if ("MARGIN_RECALC".equals(batchName) && minute % 10 == 0) {
            throw new IllegalStateException("Simulated margin snapshot timeout");
        }
        if ("POSITION_SYNC".equals(batchName) && minute % 15 == 0) {
            throw new IllegalStateException("Simulated exchange adapter latency");
        }
    }
}
