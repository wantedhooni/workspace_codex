package com.example.samplecqrs.query.api;

import com.example.samplecqrs.query.application.OrderSummaryQueryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order-summaries")
public class OrderSummaryController {

    private final OrderSummaryQueryService orderSummaryQueryService;

    public OrderSummaryController(OrderSummaryQueryService orderSummaryQueryService) {
        this.orderSummaryQueryService = orderSummaryQueryService;
    }

    @GetMapping
    public List<OrderSummaryResponse> getRecent(@RequestParam(required = false) String customerId) {
        if (customerId != null && !customerId.isBlank()) {
            return orderSummaryQueryService.getByCustomerId(customerId);
        }
        return orderSummaryQueryService.getRecent();
    }

    @GetMapping("/{orderId}")
    public OrderSummaryResponse getById(@PathVariable String orderId) {
        return orderSummaryQueryService.getById(orderId);
    }
}
