package com.commerce.service_order;

import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderRepository orders;
    private final com.commerce.service_order.events.OrderEventPublisher publisher;

    public OrderController(OrderRepository orders, com.commerce.service_order.events.OrderEventPublisher publisher) {
        this.orders = orders;
        this.publisher = publisher;
    }

    @GetMapping("/orders")
    public List<PurchaseOrder> list() {
        return orders.findAll();
    }

    @PostMapping("/orders")
    public PurchaseOrder create(@RequestBody CreateOrder request) {
        PurchaseOrder order = new PurchaseOrder(request.customerId(), request.totalAmount(), OrderStatus.CREATED);
        PurchaseOrder saved = orders.save(order);
        publisher.orderCreated(saved.getId(), saved.getCustomerId(), saved.getTotalAmount(), saved.getStatus().name());
        return saved;
    }

    @PatchMapping("/orders/{id}/status")
    public PurchaseOrder update(@PathVariable Long id, @RequestBody UpdateStatus request) {
        PurchaseOrder order = orders.findById(id).orElseThrow();
        order.setStatus(request.status());
        PurchaseOrder saved = orders.save(order);
        publisher.statusChanged(saved.getId(), saved.getCustomerId(), saved.getTotalAmount(), saved.getStatus().name());
        return saved;
    }

    public record CreateOrder(@NotNull Long customerId, @NotNull Integer totalAmount) {}
    public record UpdateStatus(@NotNull OrderStatus status) {}
}
