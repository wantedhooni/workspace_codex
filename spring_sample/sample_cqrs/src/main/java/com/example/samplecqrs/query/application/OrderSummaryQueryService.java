package com.example.samplecqrs.query.application;

import com.example.samplecqrs.query.api.OrderSummaryResponse;
import com.example.samplecqrs.query.domain.OrderSummaryView;
import com.example.samplecqrs.query.domain.OrderSummaryViewRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 읽기 모델 전용 주문 조회 서비스를 제공한다.
 */
@Service
public class OrderSummaryQueryService {

    private final OrderSummaryViewRepository orderSummaryViewRepository;

    public OrderSummaryQueryService(OrderSummaryViewRepository orderSummaryViewRepository) {
        this.orderSummaryViewRepository = orderSummaryViewRepository;
    }

    /**
     * 최근 주문 프로젝션을 조회한다.
     *
     * @return 주문 요약 목록
     */
    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> getRecent() {
        return orderSummaryViewRepository.findTop20ByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 특정 고객의 주문 프로젝션을 조회한다.
     *
     * @param customerId 고객 식별자
     * @return 주문 요약 목록
     */
    @Transactional(readOnly = true)
    public List<OrderSummaryResponse> getByCustomerId(String customerId) {
        return orderSummaryViewRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 주문 프로젝션 단건을 조회한다.
     *
     * @param orderId 주문 식별자
     * @return 주문 요약 응답
     */
    @Transactional(readOnly = true)
    public OrderSummaryResponse getById(String orderId) {
        return orderSummaryViewRepository.findById(orderId)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalStateException("주문 조회 프로젝션을 찾을 수 없습니다. orderId=" + orderId));
    }

    private OrderSummaryResponse toResponse(OrderSummaryView view) {
        return new OrderSummaryResponse(
                view.getOrderId(),
                view.getCustomerId(),
                view.getProductCode(),
                view.getQuantity(),
                view.getTotalAmount(),
                view.getStatus(),
                view.getCreatedAt(),
                view.getUpdatedAt()
        );
    }
}
