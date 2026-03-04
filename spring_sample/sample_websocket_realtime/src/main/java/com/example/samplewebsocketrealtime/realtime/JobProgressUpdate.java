package com.example.samplewebsocketrealtime.realtime;

import java.time.Instant;

public record JobProgressUpdate(
        String jobName,
        int progress,
        String status,
        Instant publishedAt
) {
}
