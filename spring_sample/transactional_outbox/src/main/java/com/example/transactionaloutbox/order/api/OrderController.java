package com.example.transactionaloutbox.order.api;

import com.example.transactionaloutbox.order.application.OrderCommandService;
import com.example.transactionaloutbox.order.application.OrderQueryService;
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
 * 주문 생성과 조회 API를 제공한다.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderCommandService orderCommandService;
    private final OrderQueryService orderQueryService;

    public OrderController(OrderCommandService orderCommandService, OrderQueryService orderQueryService) {
        this.orderCommandService = orderCommandService;
        this.orderQueryService = orderQueryService;
    }

    /**
     * 신규 주문을 생성하고 Outbox 이벤트를 함께 적재한다.
     *
     * @param request 주문 생성 요청
     * @return 생성된 주문 응답
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
        return orderCommandService.createOrder(request);
    }

    /**
     * 최근 주문 목록을 조회한다.
     *
     * @return 주문 목록
     */
    @GetMapping
    public List<OrderResponse> getRecentOrders() {
        return orderQueryService.getRecentOrders();
    }

    /**
     * 주문 단건을 조회한다.
     *
     * @param orderId 주문 식별자
     * @return 주문 응답
     */
    @GetMapping("/{orderId}")
    public OrderResponse get(@PathVariable String orderId) {
        return orderQueryService.getOrder(orderId);
    }
}
