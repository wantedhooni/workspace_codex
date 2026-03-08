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

/**
 * Saga 주문과 실행 상태를 읽기 전용으로 조회하는 서비스다.
 */
@Service
public class SagaOrderQueryService {

    private final SagaOrderRepository sagaOrderRepository;
    private final OrderSagaRepository orderSagaRepository;

    public SagaOrderQueryService(SagaOrderRepository sagaOrderRepository, OrderSagaRepository orderSagaRepository) {
        this.sagaOrderRepository = sagaOrderRepository;
        this.orderSagaRepository = orderSagaRepository;
    }

    /**
     * 주문 단건을 조회한다.
     *
     * @param orderId 주문 식별자
     * @return 주문 응답
     */
    @Transactional(readOnly = true)
    public SagaOrderResponse getOrder(String orderId) {
        SagaOrder order = sagaOrderRepository.findById(orderId)
                .orElseThrow(() -> new SagaOrderNotFoundException(orderId));
        return toResponse(order);
    }

    /**
     * 주문에 연결된 Saga 실행 정보를 조회한다.
     *
     * @param orderId 주문 식별자
     * @return Saga 실행 응답
     */
    @Transactional(readOnly = true)
    public OrderSagaResponse getSaga(String orderId) {
        OrderSaga saga = orderSagaRepository.findByOrderId(orderId)
                .orElseThrow(() -> new SagaOrderNotFoundException(orderId));
        return toResponse(saga);
    }

    /**
     * 최근 Saga 주문 목록을 조회한다.
     *
     * @return 주문 목록
     */
    @Transactional(readOnly = true)
    public List<SagaOrderResponse> getRecentOrders() {
        return sagaOrderRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 최근 Saga 실행 목록을 조회한다.
     *
     * @return Saga 실행 목록
     */
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
