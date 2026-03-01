package com.derivops.mvp.batch.application;
import com.derivops.mvp.batch.*;
import com.derivops.mvp.batch.api.*;
import com.derivops.mvp.batch.dto.*;
import com.derivops.mvp.batch.infrastructure.*;
import com.derivops.mvp.batch.config.*;
import com.derivops.mvp.batch.job.*;


import com.derivops.mvp.common.NotFoundException;
import com.derivops.mvp.common.BadRequestException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BatchService {
    private static final Set<String> ALLOWED_BATCHES = Set.of("POSITION_SYNC", "MARGIN_RECALC", "EOD_SETTLEMENT");

    private final BatchRunRepository batchRunRepository;
    private final Scheduler scheduler;
    private final JobLauncher jobLauncher;

    @Qualifier("opsBatchJob")
    private final Job opsBatchJob;

    public Page<BatchRunResponse> list(LocalDate date, BatchStatus status, String keyword, String filter, Pageable pageable) {
        return batchRunRepository.search(date, status, keyword, filter, pageable).map(this::toResponse);
    }

    public BatchRunResponse get(Long runId) {
        BatchRun run = batchRunRepository.findById(runId)
                .orElseThrow(() -> new NotFoundException("Batch run not found: " + runId));
        return toResponse(run);
    }

    private BatchRunResponse toResponse(BatchRun run) {
        return new BatchRunResponse(
                run.getId(),
                run.getBatchName(),
                run.getStatus(),
                run.getStartedAt(),
                run.getFinishedAt(),
                run.getErrorMessage(),
                run.getRetryCount()
        );
    }

    public List<BatchScheduleResponse> schedules() {
        try {
            return scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(QuartzSchedulerConfig.GROUP))
                    .stream()
                    .map(this::toScheduleResponse)
                    .sorted(Comparator.comparing(BatchScheduleResponse::triggerName))
                    .toList();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load batch schedules", ex);
        }
    }

    public void pauseSchedule(String triggerName) {
        try {
            scheduler.pauseTrigger(new TriggerKey(triggerName, QuartzSchedulerConfig.GROUP));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to pause trigger: " + triggerName, ex);
        }
    }

    public void resumeSchedule(String triggerName) {
        try {
            scheduler.resumeTrigger(new TriggerKey(triggerName, QuartzSchedulerConfig.GROUP));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to resume trigger: " + triggerName, ex);
        }
    }

    public Long runNow(String batchName, String actor) {
        if (batchName == null || batchName.isBlank() || !ALLOWED_BATCHES.contains(batchName)) {
            throw new BadRequestException("Unsupported batchName: " + batchName);
        }
        try {
            JobExecution execution = jobLauncher.run(
                    opsBatchJob,
                    new JobParametersBuilder()
                            .addString("batchName", batchName)
                            .addString("triggerSource", "MANUAL:" + actor)
                            .addLong("requestedAt", System.currentTimeMillis())
                            .toJobParameters()
            );
            return execution.getId();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to run batch now: " + batchName, ex);
        }
    }

    private BatchScheduleResponse toScheduleResponse(TriggerKey triggerKey) {
        try {
            Trigger trigger = scheduler.getTrigger(triggerKey);
            if (trigger == null) {
                throw new IllegalStateException("Missing trigger: " + triggerKey);
            }
            String batchName = scheduler.getJobDetail(trigger.getJobKey()).getJobDataMap().getString("batchName");
            String cron = trigger instanceof CronTrigger cronTrigger ? cronTrigger.getCronExpression() : null;
            OffsetDateTime previousFireTime = toOffsetDateTime(trigger.getPreviousFireTime());
            OffsetDateTime nextFireTime = toOffsetDateTime(trigger.getNextFireTime());
            String state = scheduler.getTriggerState(triggerKey).name();
            return new BatchScheduleResponse(
                    triggerKey.getName(),
                    trigger.getJobKey().getName(),
                    batchName,
                    cron,
                    previousFireTime,
                    nextFireTime,
                    state
            );
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to resolve trigger: " + triggerKey, ex);
        }
    }

    private OffsetDateTime toOffsetDateTime(java.util.Date date) {
        if (date == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneOffset.UTC);
    }
}
