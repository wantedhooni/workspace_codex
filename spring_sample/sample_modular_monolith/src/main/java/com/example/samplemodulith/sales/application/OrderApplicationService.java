package com.example.samplemodulith.sales.application;

import com.example.samplemodulith.sales.domain.SalesOrder;
import com.example.samplemodulith.sales.domain.SalesOrderRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderApplicationService {

    private final SalesOrderRepository salesOrderRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public OrderApplicationService(
            SalesOrderRepository salesOrderRepository,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.salesOrderRepository = salesOrderRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public SalesOrder placeOrder(PlaceOrderRequest request) {
        SalesOrder order = salesOrderRepository.save(
                new SalesOrder(request.customerId(), request.sku(), request.quantity(), request.unitPrice())
        );

        applicationEventPublisher.publishEvent(
                new OrderPlacedEvent(order.getId(), order.getCustomerId(), order.getSku(), order.getQuantity(), order.getUnitPrice())
        );

        return order;
    }
}
