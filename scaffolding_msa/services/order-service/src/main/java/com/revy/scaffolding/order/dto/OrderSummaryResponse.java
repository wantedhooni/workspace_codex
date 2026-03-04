package com.revy.scaffolding.order.dto;

import com.revy.scaffolding.order.domain.OrderStatus;
import com.revy.scaffolding.order.domain.PurchaseOrder;
import java.math.BigDecimal;

public record OrderSummaryResponse(
    Long id,
    String orderNo,
    Long userId,
    BigDecimal amount,
    OrderStatus status
) {
    public static OrderSummaryResponse from(PurchaseOrder order) {
        return new OrderSummaryResponse(
            order.getId(),
            order.getOrderNo(),
            order.getUserId(),
            order.getAmount(),
            order.getStatus()
        );
    }
}

