package com.example.samplebatchquartzdashboard.dashboard;

import java.time.Instant;

public record QuartzStatusResponse(
        boolean started,
        boolean standbyMode,
        boolean shutdown,
        String schedulerState,
        String triggerState,
        String cronExpression,
        Instant previousFireTime,
        Instant nextFireTime
) {
}
