package com.derivops.mvp.batch.dto;
import com.derivops.mvp.batch.*;
import com.derivops.mvp.batch.api.*;
import com.derivops.mvp.batch.application.*;
import com.derivops.mvp.batch.infrastructure.*;
import com.derivops.mvp.batch.config.*;
import com.derivops.mvp.batch.job.*;


import java.time.OffsetDateTime;

public record BatchScheduleResponse(
        String triggerName,
        String jobName,
        String batchName,
        String cronExpression,
        OffsetDateTime previousFireTime,
        OffsetDateTime nextFireTime,
        String triggerState
) {
}
