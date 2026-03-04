package com.example.samplemodulith.billing.application;

import com.example.samplemodulith.billing.domain.Invoice;
import com.example.samplemodulith.billing.domain.InvoiceRepository;
import com.example.samplemodulith.inventory.application.InventoryReservedEvent;
import com.example.samplemodulith.sales.domain.SalesOrderRepository;
import java.math.BigDecimal;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BillingService {

    private final InvoiceRepository invoiceRepository;
    private final SalesOrderRepository salesOrderRepository;

    public BillingService(InvoiceRepository invoiceRepository, SalesOrderRepository salesOrderRepository) {
        this.invoiceRepository = invoiceRepository;
        this.salesOrderRepository = salesOrderRepository;
    }

    @Transactional
    @EventListener
    public void handle(InventoryReservedEvent event) {
        var order = salesOrderRepository.findById(event.orderId()).orElseThrow();
        BigDecimal amount = order.getUnitPrice().multiply(BigDecimal.valueOf(order.getQuantity()));

        invoiceRepository.findByOrderId(order.getId()).orElseGet(() -> invoiceRepository.save(new Invoice(order.getId(), amount)));
        order.markInvoiced();
        salesOrderRepository.save(order);
    }
}
