package com.example.samplebatchquartzdashboard.dashboard;

import java.time.Instant;

public record QuartzActionResponse(
        String action,
        String schedulerState,
        String triggerState,
        Instant nextFireTime
) {
}
