package com.example.marketsignal.batch;

import java.time.OffsetDateTime;

/**
 * Quartz JDBC 트리거 메타데이터를 운영 화면에 전달한다.
 */
public record QuartzTriggerMetadataResponse(
        String triggerName,
        String triggerGroup,
        String triggerState,
        String cronExpression,
        String zoneId,
        OffsetDateTime startTime,
        OffsetDateTime nextFireTime,
        OffsetDateTime previousFireTime,
        OffsetDateTime endTime
) {
}
