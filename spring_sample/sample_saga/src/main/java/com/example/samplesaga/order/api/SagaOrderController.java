package com.example.samplesaga.order.api;

import com.example.samplesaga.order.application.SagaOrderQueryService;
import com.example.samplesaga.saga.api.OrderSagaResponse;
import com.example.samplesaga.saga.application.OrderSagaOrchestrator;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SagaOrderResultResponse create(@Valid @RequestBody CreateSagaOrderRequest request) {
        return orderSagaOrchestrator.start(request);
    }

    @GetMapping("/{orderId}")
    public SagaOrderResponse getOrder(@PathVariable String orderId) {
        return sagaOrderQueryService.getOrder(orderId);
    }

    @GetMapping("/{orderId}/saga")
    public OrderSagaResponse getSaga(@PathVariable String orderId) {
        return sagaOrderQueryService.getSaga(orderId);
    }
}
