package com.example.marketsignal.batch;

import java.util.List;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring Batch와 Quartz 운영 제어용 REST API를 제공한다.
 */
@RestController
@Validated
@RequestMapping("/api/batch/jobs")
@RequiredArgsConstructor
public class BatchOperationsController {

    private final BatchOperationsService batchOperationsService;
    private final BatchJdbcMetadataService batchJdbcMetadataService;

    /**
     * 관리 대상 배치 작업 목록과 현재 상태를 조회한다.
     */
    @GetMapping
    public List<BatchJobStatusResponse> getJobs() {
        return batchOperationsService.getManagedJobs();
    }

    /**
     * 관리 대상 배치 작업 한 건의 현재 상태를 조회한다.
     */
    @GetMapping("/{jobName}")
    public BatchJobStatusResponse getJob(@PathVariable String jobName) {
        return batchOperationsService.getManagedJob(jobName);
    }

    /**
     * 관리 대상 배치 작업의 JDBC 실행 이력과 Quartz 메타데이터를 조회한다.
     */
    @GetMapping("/{jobName}/metadata")
    public BatchJobMetadataResponse getJobMetadata(
            @PathVariable String jobName,
            @RequestParam(defaultValue = "10") @Min(1) @Max(20) int limit
    ) {
        return batchJdbcMetadataService.getJobMetadata(jobName, limit);
    }

    /**
     * 특정 배치 작업을 즉시 실행한다.
     */
    @PostMapping("/{jobName}/run")
    public BatchJobStatusResponse runJob(@PathVariable String jobName) {
        return batchOperationsService.runJob(jobName);
    }

    /**
     * 특정 Quartz 스케줄을 일시 정지한다.
     */
    @PostMapping("/{jobName}/pause")
    public BatchJobStatusResponse pauseJob(@PathVariable String jobName) {
        return batchOperationsService.pauseSchedule(jobName);
    }

    /**
     * 특정 Quartz 스케줄을 재개한다.
     */
    @PostMapping("/{jobName}/resume")
    public BatchJobStatusResponse resumeJob(@PathVariable String jobName) {
        return batchOperationsService.resumeSchedule(jobName);
    }
}
