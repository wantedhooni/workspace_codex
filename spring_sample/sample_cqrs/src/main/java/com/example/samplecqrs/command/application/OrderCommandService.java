package com.example.samplecqrs.command.application;

import com.example.samplecqrs.command.api.CreateOrderRequest;
import com.example.samplecqrs.command.api.OrderCommandResponse;
import com.example.samplecqrs.command.domain.PurchaseOrder;
import com.example.samplecqrs.command.domain.PurchaseOrderRepository;
import com.example.samplecqrs.events.OrderCancelledEvent;
import com.example.samplecqrs.events.OrderCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderCommandService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public OrderCommandService(
            PurchaseOrderRepository purchaseOrderRepository,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public OrderCommandResponse createOrder(CreateOrderRequest request) {
        PurchaseOrder order = purchaseOrderRepository.save(
                new PurchaseOrder(request.customerId(), request.productCode(), request.quantity(), request.unitPrice())
        );
        applicationEventPublisher.publishEvent(OrderCreatedEvent.from(order));
        return OrderCommandResponseMapper.toResponse(order);
    }

    @Transactional
    public OrderCommandResponse cancelOrder(String orderId) {
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        order.cancel();
        applicationEventPublisher.publishEvent(OrderCancelledEvent.from(order));
        return OrderCommandResponseMapper.toResponse(order);
    }
}
