package com.revy.scaffolding.order.api;

import com.revy.scaffolding.core.api.ApiResponse;
import com.revy.scaffolding.order.dto.OrderCreateRequest;
import com.revy.scaffolding.order.dto.OrderDetailResponse;
import com.revy.scaffolding.order.dto.OrderSummaryResponse;
import com.revy.scaffolding.order.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ApiResponse<OrderDetailResponse> create(@Valid @RequestBody OrderCreateRequest request) {
        return ApiResponse.ok(orderService.create(request));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderDetailResponse> get(@PathVariable Long orderId) {
        return ApiResponse.ok(orderService.get(orderId));
    }

    @GetMapping
    public ApiResponse<List<OrderSummaryResponse>> list(@RequestParam(required = false) Long userId) {
        return ApiResponse.ok(orderService.list(userId));
    }
}
