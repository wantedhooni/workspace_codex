package com.example.marketsignal.batch;

import com.example.marketsignal.common.BusinessException;
import com.example.marketsignal.config.AppProperties;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

/**
 * Spring Batch 작업 실행과 Quartz 스케줄 제어를 운영 API 관점에서 제공한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BatchOperationsService {

    private static final int RECENT_EXECUTION_LIMIT = 5;

    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;
    private final Scheduler scheduler;
    private final Map<String, Job> jobs;
    private final AppProperties appProperties;

    /**
     * 운영 화면에서 관리할 전체 배치 작업 상태를 조회한다.
     */
    public List<BatchJobStatusResponse> getManagedJobs() {
        return List.of(ManagedBatchJob.values()).stream()
                .map(this::buildJobStatus)
                .toList();
    }

    /**
     * 운영 API에서 특정 배치 작업을 즉시 실행한다.
     */
    public BatchJobStatusResponse runJob(String jobName) {
        ManagedBatchJob managedJob = ManagedBatchJob.fromApiName(jobName);
        launchJob(managedJob, "manual");
        return buildJobStatus(managedJob);
    }

    /**
     * 애플리케이션 시작 시 필요한 초기 배치를 실행한다.
     */
    public void launchStartupJob(String jobName) {
        launchJob(ManagedBatchJob.fromApiName(jobName), "startup");
    }

    /**
     * Quartz 트리거에서 호출되는 스케줄 배치를 실행한다.
     */
    public void launchScheduledJob(String jobName) {
        launchJob(ManagedBatchJob.fromApiName(jobName), "quartz");
    }

    /**
     * 특정 Quartz 스케줄을 일시 정지한다.
     */
    public BatchJobStatusResponse pauseSchedule(String jobName) {
        ManagedBatchJob managedJob = requireSchedulableJob(jobName);

        try {
            scheduler.pauseTrigger(managedJob.quartzTriggerKey());
            return buildJobStatus(managedJob);
        } catch (Exception exception) {
            log.error("배치 스케줄 일시 정지에 실패했습니다. job={}", managedJob.apiName(), exception);
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "배치 스케줄을 일시 정지하지 못했습니다.", exception);
        }
    }

    /**
     * 특정 Quartz 스케줄을 재개한다.
     */
    public BatchJobStatusResponse resumeSchedule(String jobName) {
        ManagedBatchJob managedJob = requireSchedulableJob(jobName);

        try {
            scheduler.resumeTrigger(managedJob.quartzTriggerKey());
            return buildJobStatus(managedJob);
        } catch (Exception exception) {
            log.error("배치 스케줄 재개에 실패했습니다. job={}", managedJob.apiName(), exception);
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "배치 스케줄을 재개하지 못했습니다.", exception);
        }
    }

    /**
     * 관리 대상 배치 한 건의 현재 상태를 조회한다.
     */
    public BatchJobStatusResponse getManagedJob(String jobName) {
        return buildJobStatus(ManagedBatchJob.fromApiName(jobName));
    }

    /**
     * 실제 Spring Batch 작업을 파라미터와 함께 실행한다.
     */
    private void launchJob(ManagedBatchJob managedJob, String requestedBy) {
        Set<JobExecution> runningExecutions = jobExplorer.findRunningJobExecutions(managedJob.springBatchJobName());
        if (!runningExecutions.isEmpty()) {
            throw new BusinessException(HttpStatus.CONFLICT, "이미 실행 중인 배치 작업입니다.");
        }

        Job job = Optional.ofNullable(jobs.get(managedJob.springBatchJobName()))
                .orElseThrow(() -> new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "등록된 Spring Batch 작업을 찾을 수 없습니다."));

        try {
            JobExecution execution = jobLauncher.run(job, new JobParametersBuilder()
                    .addLong("requestedAt", System.currentTimeMillis())
                    .addString("requestedBy", requestedBy)
                    .addString("managedJobName", managedJob.apiName())
                    .toJobParameters());
            log.info("배치 작업을 실행했습니다. job={}, executionId={}, requestedBy={}", managedJob.apiName(), execution.getId(), requestedBy);
        } catch (JobExecutionAlreadyRunningException exception) {
            throw new BusinessException(HttpStatus.CONFLICT, "이미 실행 중인 배치 작업입니다.");
        } catch (JobInstanceAlreadyCompleteException exception) {
            throw new BusinessException(HttpStatus.CONFLICT, "이미 완료된 동일 배치 실행 요청입니다.");
        } catch (JobRestartException exception) {
            throw new BusinessException(HttpStatus.CONFLICT, "현재 상태에서는 배치 작업을 재시작할 수 없습니다.");
        } catch (Exception exception) {
            log.error("배치 작업 실행에 실패했습니다. job={}, springBatchJob={}, requestedBy={}",
                    managedJob.apiName(), managedJob.springBatchJobName(), requestedBy, exception);
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "배치 작업 실행에 실패했습니다.", exception);
        }
    }

    /**
     * 현재 배치 작업의 실행 이력과 Quartz 스케줄 정보를 응답 모델로 변환한다.
     */
    private BatchJobStatusResponse buildJobStatus(ManagedBatchJob managedJob) {
        List<JobExecution> recentExecutions = getRecentExecutions(managedJob);
        BatchExecutionResponse lastExecution = recentExecutions.isEmpty() ? null : toExecutionResponse(recentExecutions.getFirst());
        boolean running = recentExecutions.stream().anyMatch(JobExecution::isRunning);

        if (!managedJob.schedulable()) {
            return new BatchJobStatusResponse(
                    managedJob.apiName(),
                    managedJob.title(),
                    managedJob.description(),
                    false,
                    "MANUAL_ONLY",
                    null,
                    null,
                    null,
                    null,
                    running,
                    lastExecution,
                    recentExecutions.stream().map(this::toExecutionResponse).toList()
            );
        }

        try {
            TriggerKey triggerKey = managedJob.quartzTriggerKey();
            Trigger trigger = scheduler.getTrigger(triggerKey);
            Trigger.TriggerState triggerState = scheduler.getTriggerState(triggerKey);
            CronTrigger cronTrigger = trigger instanceof CronTrigger currentCronTrigger ? currentCronTrigger : null;

            return new BatchJobStatusResponse(
                    managedJob.apiName(),
                    managedJob.title(),
                    managedJob.description(),
                    true,
                    mapScheduleState(triggerState),
                    cronTrigger != null ? cronTrigger.getCronExpression() : appProperties.batch().report().cron(),
                    cronTrigger != null ? cronTrigger.getTimeZone().toZoneId().getId() : appProperties.batch().report().zoneId(),
                    toOffsetDateTime(trigger != null ? trigger.getNextFireTime() : null),
                    toOffsetDateTime(trigger != null ? trigger.getPreviousFireTime() : null),
                    running,
                    lastExecution,
                    recentExecutions.stream().map(this::toExecutionResponse).toList()
            );
        } catch (Exception exception) {
            log.error("배치 작업 상태 조회에 실패했습니다. job={}", managedJob.apiName(), exception);
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "배치 작업 상태를 조회하지 못했습니다.", exception);
        }
    }

    /**
     * JobExplorer 메타데이터에서 최신 실행 이력을 수집한다.
     */
    private List<JobExecution> getRecentExecutions(ManagedBatchJob managedJob) {
        return jobExplorer.getJobInstances(managedJob.springBatchJobName(), 0, RECENT_EXECUTION_LIMIT).stream()
                .flatMap(jobInstance -> jobExplorer.getJobExecutions(jobInstance).stream())
                .sorted(Comparator.comparing(this::executionSortTime, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .limit(RECENT_EXECUTION_LIMIT)
                .toList();
    }

    /**
     * JobExecution 정렬 기준 시각을 계산한다.
     */
    private LocalDateTime executionSortTime(JobExecution execution) {
        if (execution.getStartTime() != null) {
            return execution.getStartTime();
        }
        if (execution.getCreateTime() != null) {
            return execution.getCreateTime();
        }
        return execution.getLastUpdated();
    }

    /**
     * Batch JobExecution 메타데이터를 API 응답 모델로 변환한다.
     */
    private BatchExecutionResponse toExecutionResponse(JobExecution execution) {
        return new BatchExecutionResponse(
                execution.getId(),
                execution.getStatus().name(),
                execution.getExitStatus().getExitCode(),
                execution.getExitStatus().getExitDescription(),
                toOffsetDateTime(execution.getStartTime()),
                toOffsetDateTime(execution.getEndTime())
        );
    }

    /**
     * Quartz Trigger 상태를 운영 화면용 문자열로 변환한다.
     */
    private String mapScheduleState(Trigger.TriggerState triggerState) {
        return switch (triggerState) {
            case NORMAL, BLOCKED -> "SCHEDULED";
            case PAUSED -> "PAUSED";
            case COMPLETE -> "COMPLETED";
            case ERROR -> "ERROR";
            case NONE -> "NOT_FOUND";
        };
    }

    /**
     * 수동 전용 작업에 pause/resume 요청이 들어온 경우를 방어한다.
     */
    private ManagedBatchJob requireSchedulableJob(String jobName) {
        ManagedBatchJob managedJob = ManagedBatchJob.fromApiName(jobName);
        if (!managedJob.schedulable()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "해당 배치 작업은 수동 실행만 지원합니다.");
        }
        return managedJob;
    }

    /**
     * Quartz Date 값을 서버 표준 시간대 기준 OffsetDateTime으로 변환한다.
     */
    private OffsetDateTime toOffsetDateTime(Date value) {
        if (value == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(value.toInstant(), ZoneId.of(appProperties.batch().report().zoneId()));
    }

    /**
     * Batch LocalDateTime 값을 서버 표준 시간대 기준 OffsetDateTime으로 변환한다.
     */
    private OffsetDateTime toOffsetDateTime(LocalDateTime value) {
        if (value == null) {
            return null;
        }
        ZoneId zoneId = ZoneId.of(appProperties.batch().report().zoneId());
        Instant instant = value.atZone(zoneId).toInstant();
        return OffsetDateTime.ofInstant(instant, zoneId);
    }
}
