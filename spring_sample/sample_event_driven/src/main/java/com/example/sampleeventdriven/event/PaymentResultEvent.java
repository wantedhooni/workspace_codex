package com.example.sampleeventdriven.event;

import java.math.BigDecimal;

public record PaymentResultEvent(
        Long orderId,
        String accountId,
        BigDecimal amount,
        String result
) {
}
