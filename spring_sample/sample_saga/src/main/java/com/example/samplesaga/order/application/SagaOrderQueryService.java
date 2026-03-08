package com.example.samplesaga.order.application;

import com.example.samplesaga.order.api.SagaOrderResponse;
import com.example.samplesaga.order.domain.SagaOrder;
import com.example.samplesaga.order.domain.SagaOrderRepository;
import com.example.samplesaga.saga.api.OrderSagaResponse;
import com.example.samplesaga.saga.domain.OrderSaga;
import com.example.samplesaga.saga.domain.OrderSagaRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SagaOrderQueryService {

    private final SagaOrderRepository sagaOrderRepository;
    private final OrderSagaRepository orderSagaRepository;

    public SagaOrderQueryService(SagaOrderRepository sagaOrderRepository, OrderSagaRepository orderSagaRepository) {
        this.sagaOrderRepository = sagaOrderRepository;
        this.orderSagaRepository = orderSagaRepository;
    }

    @Transactional(readOnly = true)
    public SagaOrderResponse getOrder(String orderId) {
        SagaOrder order = sagaOrderRepository.findById(orderId)
                .orElseThrow(() -> new SagaOrderNotFoundException(orderId));
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderSagaResponse getSaga(String orderId) {
        OrderSaga saga = orderSagaRepository.findByOrderId(orderId)
                .orElseThrow(() -> new SagaOrderNotFoundException(orderId));
        return toResponse(saga);
    }

    @Transactional(readOnly = true)
    public List<SagaOrderResponse> getRecentOrders() {
        return sagaOrderRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderSagaResponse> getRecentSagaExecutions() {
        return orderSagaRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    public SagaOrderResponse toResponse(SagaOrder order) {
        return new SagaOrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getProductCode(),
                order.getQuantity(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getFailureReason(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    public OrderSagaResponse toResponse(OrderSaga saga) {
        return new OrderSagaResponse(
                saga.getSagaId(),
                saga.getOrderId(),
                saga.getStatus(),
                saga.getPaymentStatus(),
                saga.getInventoryStatus(),
                saga.getFailureReason(),
                saga.getCreatedAt(),
                saga.getUpdatedAt()
        );
    }
}
