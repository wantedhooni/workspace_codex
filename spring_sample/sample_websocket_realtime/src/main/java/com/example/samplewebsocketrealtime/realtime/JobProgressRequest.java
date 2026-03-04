package com.example.samplewebsocketrealtime.realtime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record JobProgressRequest(
        @NotBlank String jobName,
        @Min(0) @Max(100) int progress,
        @NotBlank String status
) {
}
