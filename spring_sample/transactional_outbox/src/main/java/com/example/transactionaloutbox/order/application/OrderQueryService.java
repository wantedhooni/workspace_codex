package com.example.transactionaloutbox.order.application;

import com.example.transactionaloutbox.order.api.OrderResponse;
import com.example.transactionaloutbox.order.domain.PurchaseOrder;
import com.example.transactionaloutbox.order.domain.PurchaseOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderQueryService {

    private final PurchaseOrderRepository purchaseOrderRepository;

    public OrderQueryService(PurchaseOrderRepository purchaseOrderRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderId) {
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return OrderResponseMapper.toResponse(order);
    }
}
