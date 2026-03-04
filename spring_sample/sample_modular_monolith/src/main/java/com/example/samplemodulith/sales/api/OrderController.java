package com.example.samplemodulith.sales.api;

import com.example.samplemodulith.sales.application.OrderApplicationService;
import com.example.samplemodulith.sales.application.PlaceOrderRequest;
import com.example.samplemodulith.sales.domain.SalesOrderRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderApplicationService orderApplicationService;
    private final SalesOrderRepository salesOrderRepository;

    public OrderController(OrderApplicationService orderApplicationService, SalesOrderRepository salesOrderRepository) {
        this.orderApplicationService = orderApplicationService;
        this.salesOrderRepository = salesOrderRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse place(@Valid @RequestBody PlaceOrderRequest request) {
        var order = orderApplicationService.placeOrder(request);
        return toResponse(order.getId());
    }

    @GetMapping("/{orderId}")
    public OrderResponse get(@PathVariable String orderId) {
        return toResponse(orderId);
    }

    private OrderResponse toResponse(String orderId) {
        var order = salesOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));
        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getSku(),
                order.getQuantity(),
                order.getUnitPrice(),
                order.getStatus()
        );
    }
}
