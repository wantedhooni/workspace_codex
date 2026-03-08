package com.example.samplecqrs.query.application;

import com.example.samplecqrs.events.OrderCancelledEvent;
import com.example.samplecqrs.events.OrderCreatedEvent;
import com.example.samplecqrs.query.domain.OrderSummaryView;
import com.example.samplecqrs.query.domain.OrderSummaryViewRepository;
import org.springframework.stereotype.Component;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OrderProjectionUpdater {

    private final OrderSummaryViewRepository orderSummaryViewRepository;

    public OrderProjectionUpdater(OrderSummaryViewRepository orderSummaryViewRepository) {
        this.orderSummaryViewRepository = orderSummaryViewRepository;
    }

    @Transactional
    @EventListener
    public void handle(OrderCreatedEvent event) {
        orderSummaryViewRepository.save(new OrderSummaryView(
                event.orderId(),
                event.customerId(),
                event.productCode(),
                event.quantity(),
                event.totalAmount(),
                event.status(),
                event.occurredAt(),
                event.occurredAt()
        ));
    }

    @Transactional
    @EventListener
    public void handle(OrderCancelledEvent event) {
        OrderSummaryView view = orderSummaryViewRepository.findById(event.orderId())
                .orElseThrow(() -> new IllegalStateException("주문 프로젝션을 찾을 수 없습니다. orderId=" + event.orderId()));
        view.markCancelled(event.occurredAt());
        orderSummaryViewRepository.save(view);
    }
}
