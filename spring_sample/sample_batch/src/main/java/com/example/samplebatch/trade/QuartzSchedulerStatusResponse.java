package com.example.samplebatch.trade;

import java.time.Instant;

public record QuartzSchedulerStatusResponse(
        String jobName,
        String triggerName,
        String triggerState,
        Instant previousFireTime,
        Instant nextFireTime
) {
}
