package com.example.samplemodulith.inventory.application;

import com.example.samplemodulith.inventory.domain.InventoryItemRepository;
import com.example.samplemodulith.sales.application.OrderPlacedEvent;
import com.example.samplemodulith.sales.domain.SalesOrderRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryReservationService {

    private final InventoryItemRepository inventoryItemRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public InventoryReservationService(
            InventoryItemRepository inventoryItemRepository,
            SalesOrderRepository salesOrderRepository,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    @EventListener
    public void handle(OrderPlacedEvent event) {
        var order = salesOrderRepository.findById(event.orderId()).orElseThrow();
        var inventory = inventoryItemRepository.findById(event.sku()).orElseThrow();

        if (!inventory.canReserve(event.quantity())) {
            order.markRejected();
            salesOrderRepository.save(order);
            return;
        }

        inventory.reserve(event.quantity());
        order.markReserved();

        inventoryItemRepository.save(inventory);
        salesOrderRepository.save(order);
        applicationEventPublisher.publishEvent(new InventoryReservedEvent(order.getId()));
    }
}
