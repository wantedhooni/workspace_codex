package com.tradingmacro.macro;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record MacroJobRequest(
    @NotBlank String name,
    String schedule,
    @NotNull MacroJob.Status status,
    Instant lastRun,
    Instant nextRun,
    Long strategyId
) {}
