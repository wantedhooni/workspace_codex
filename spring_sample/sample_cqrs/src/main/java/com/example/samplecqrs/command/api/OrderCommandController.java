package com.example.samplecqrs.command.api;

import com.example.samplecqrs.command.application.OrderCommandService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 주문 생성과 취소 같은 쓰기 모델 명령 API를 제공한다.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderCommandController {

    private final OrderCommandService orderCommandService;

    public OrderCommandController(OrderCommandService orderCommandService) {
        this.orderCommandService = orderCommandService;
    }

    /**
     * 신규 주문을 생성한다.
     *
     * @param request 주문 생성 요청
     * @return 생성된 주문 정보
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderCommandResponse create(@Valid @RequestBody CreateOrderRequest request) {
        return orderCommandService.createOrder(request);
    }

    /**
     * 기존 주문을 취소한다.
     *
     * @param orderId 주문 식별자
     * @return 취소 반영 후 주문 정보
     */
    @PostMapping("/{orderId}/cancel")
    public OrderCommandResponse cancel(@PathVariable String orderId) {
        return orderCommandService.cancelOrder(orderId);
    }
}
