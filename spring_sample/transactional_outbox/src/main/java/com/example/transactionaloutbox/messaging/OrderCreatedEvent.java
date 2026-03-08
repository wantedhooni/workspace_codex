package com.example.transactionaloutbox.messaging;

import com.example.transactionaloutbox.order.domain.PurchaseOrder;
import java.math.BigDecimal;
import java.time.Instant;

public record OrderCreatedEvent(
        String orderId,
        String customerId,
        String productId,
        int quantity,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        Instant createdAt
) {

    public static OrderCreatedEvent from(PurchaseOrder order) {
        return new OrderCreatedEvent(
                order.getId(),
                order.getCustomerId(),
                order.getProductId(),
                order.getQuantity(),
                order.getUnitPrice(),
                order.getTotalAmount(),
                order.getCreatedAt()
        );
    }
}
