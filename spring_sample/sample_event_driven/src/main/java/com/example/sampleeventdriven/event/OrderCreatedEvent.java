package com.example.sampleeventdriven.event;

import java.math.BigDecimal;

public record OrderCreatedEvent(
        Long orderId,
        String accountId,
        BigDecimal amount
) {
}
