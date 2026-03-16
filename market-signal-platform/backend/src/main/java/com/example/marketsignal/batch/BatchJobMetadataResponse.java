package com.example.marketsignal.batch;

import java.util.List;

/**
 * 관리 대상 배치 작업의 JDBC 메타데이터 상세를 운영 화면에 전달한다.
 */
public record BatchJobMetadataResponse(
        String jobName,
        String title,
        String description,
        BatchJdbcMetricsResponse metrics,
        QuartzTriggerMetadataResponse quartzTrigger,
        List<BatchExecutionDetailResponse> executions
) {
}
