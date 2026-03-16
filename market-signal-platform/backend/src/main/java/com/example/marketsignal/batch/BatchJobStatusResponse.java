package com.example.marketsignal.batch;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 관리 대상 배치 작업의 현재 스케줄과 실행 상태를 운영 화면에 전달한다.
 */
public record BatchJobStatusResponse(
        String jobName,
        String title,
        String description,
        boolean schedulable,
        String scheduleState,
        String cronExpression,
        String zoneId,
        OffsetDateTime nextFireTime,
        OffsetDateTime previousFireTime,
        boolean running,
        BatchExecutionResponse lastExecution,
        List<BatchExecutionResponse> recentExecutions
) {
}
