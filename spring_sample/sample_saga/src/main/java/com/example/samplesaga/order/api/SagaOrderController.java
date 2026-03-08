package com.example.samplesaga.order.api;

import com.example.samplesaga.order.application.SagaOrderQueryService;
import com.example.samplesaga.saga.api.OrderSagaResponse;
import com.example.samplesaga.saga.application.OrderSagaOrchestrator;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Saga 주문 생성과 조회 API를 제공한다.
 */
@RestController
@RequestMapping("/api/saga/orders")
public class SagaOrderController {

    private final OrderSagaOrchestrator orderSagaOrchestrator;
    private final SagaOrderQueryService sagaOrderQueryService;

    public SagaOrderController(
            OrderSagaOrchestrator orderSagaOrchestrator,
            SagaOrderQueryService sagaOrderQueryService
    ) {
        this.orderSagaOrchestrator = orderSagaOrchestrator;
        this.sagaOrderQueryService = sagaOrderQueryService;
    }

    /**
     * 신규 Saga 주문을 시작한다.
     *
     * @param request 주문 생성 요청
     * @return 주문과 Saga 상태 응답
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SagaOrderResultResponse create(@Valid @RequestBody CreateSagaOrderRequest request) {
        return orderSagaOrchestrator.start(request);
    }

    /**
     * 최근 Saga 주문 목록을 조회한다.
     *
     * @return 주문 목록
     */
    @GetMapping
    public List<SagaOrderResponse> getRecentOrders() {
        return sagaOrderQueryService.getRecentOrders();
    }

    /**
     * 주문 단건을 조회한다.
     *
     * @param orderId 주문 식별자
     * @return 주문 응답
     */
    @GetMapping("/{orderId}")
    public SagaOrderResponse getOrder(@PathVariable String orderId) {
        return sagaOrderQueryService.getOrder(orderId);
    }

    /**
     * 최근 Saga 실행 목록을 조회한다.
     *
     * @return Saga 실행 목록
     */
    @GetMapping("/executions")
    public List<OrderSagaResponse> getRecentSagas() {
        return sagaOrderQueryService.getRecentSagaExecutions();
    }

    /**
     * 주문에 연결된 Saga 실행 정보를 조회한다.
     *
     * @param orderId 주문 식별자
     * @return Saga 실행 응답
     */
    @GetMapping("/{orderId}/saga")
    public OrderSagaResponse getSaga(@PathVariable String orderId) {
        return sagaOrderQueryService.getSaga(orderId);
    }
}
