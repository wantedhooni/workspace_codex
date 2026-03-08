package com.example.transactionaloutbox.order.application;

import com.example.transactionaloutbox.order.api.OrderResponse;
import com.example.transactionaloutbox.order.domain.PurchaseOrder;
import com.example.transactionaloutbox.order.domain.PurchaseOrderRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 주문 조회 전용 서비스를 제공한다.
 */
@Service
public class OrderQueryService {

    private final PurchaseOrderRepository purchaseOrderRepository;

    public OrderQueryService(PurchaseOrderRepository purchaseOrderRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
    }

    /**
     * 주문 단건을 조회한다.
     *
     * @param orderId 주문 식별자
     * @return 주문 응답
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrder(String orderId) {
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return OrderResponseMapper.toResponse(order);
    }

    /**
     * 최근 주문 목록을 조회한다.
     *
     * @return 주문 목록
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getRecentOrders() {
        return purchaseOrderRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(OrderResponseMapper::toResponse)
                .toList();
    }
}
