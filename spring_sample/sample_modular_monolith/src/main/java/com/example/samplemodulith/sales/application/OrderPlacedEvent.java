package com.example.samplemodulith.sales.application;

import java.math.BigDecimal;

public record OrderPlacedEvent(
        String orderId,
        String customerId,
        String sku,
        int quantity,
        BigDecimal unitPrice
) {
}
