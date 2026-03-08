package com.example.transactionaloutbox.order.application;

import com.example.transactionaloutbox.order.api.OrderResponse;
import com.example.transactionaloutbox.order.domain.PurchaseOrder;

public final class OrderResponseMapper {

    private OrderResponseMapper() {
    }

    public static OrderResponse toResponse(PurchaseOrder order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getProductId(),
                order.getQuantity(),
                order.getUnitPrice(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}
