package com.example.samplewebsocketrealtime.realtime;

import java.math.BigDecimal;
import java.time.Instant;

public record PriceUpdate(
        String instrument,
        BigDecimal price,
        Instant publishedAt
) {
}
