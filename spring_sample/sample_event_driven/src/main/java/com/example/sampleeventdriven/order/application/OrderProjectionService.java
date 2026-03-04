package com.example.sampleeventdriven.order.application;

import com.example.sampleeventdriven.event.PaymentResultEvent;
import com.example.sampleeventdriven.order.domain.CustomerOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderProjectionService {

    private final CustomerOrderRepository customerOrderRepository;

    public OrderProjectionService(CustomerOrderRepository customerOrderRepository) {
        this.customerOrderRepository = customerOrderRepository;
    }

    @Transactional
    public void apply(PaymentResultEvent event) {
        var order = customerOrderRepository.findById(event.orderId()).orElseThrow();
        if ("AUTHORIZED".equals(event.result())) {
            order.markPaid();
        } else {
            order.markPaymentFailed();
        }
        customerOrderRepository.save(order);
    }
}
