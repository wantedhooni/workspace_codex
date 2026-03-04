package com.example.samplemodulith.sales.api;

import com.example.samplemodulith.sales.domain.OrderStatus;
import java.math.BigDecimal;

public record OrderResponse(
        String orderId,
        String customerId,
        String sku,
        int quantity,
        BigDecimal unitPrice,
        OrderStatus status
) {
}
