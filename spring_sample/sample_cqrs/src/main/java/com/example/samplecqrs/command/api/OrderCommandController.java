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

@RestController
@RequestMapping("/api/orders")
public class OrderCommandController {

    private final OrderCommandService orderCommandService;

    public OrderCommandController(OrderCommandService orderCommandService) {
        this.orderCommandService = orderCommandService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderCommandResponse create(@Valid @RequestBody CreateOrderRequest request) {
        return orderCommandService.createOrder(request);
    }

    @PostMapping("/{orderId}/cancel")
    public OrderCommandResponse cancel(@PathVariable String orderId) {
        return orderCommandService.cancelOrder(orderId);
    }
}
