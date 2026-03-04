package com.example.samplemodulith;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.samplemodulith.billing.domain.InvoiceRepository;
import com.example.samplemodulith.inventory.domain.InventoryItemRepository;
import com.example.samplemodulith.sales.application.OrderApplicationService;
import com.example.samplemodulith.sales.application.PlaceOrderRequest;
import com.example.samplemodulith.sales.domain.OrderStatus;
import com.example.samplemodulith.sales.domain.SalesOrderRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ModularMonolithFlowTests {

    @Autowired
    private OrderApplicationService orderApplicationService;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Test
    void placingOrderReservesInventoryAndIssuesInvoice() throws Exception {
        var order = orderApplicationService.placeOrder(
                new PlaceOrderRequest("CUS-100", "AAPL", 10, new BigDecimal("182.50"))
        );

        var savedOrder = salesOrderRepository.findById(order.getId()).orElseThrow();
        var inventory = inventoryItemRepository.findById("AAPL").orElseThrow();

        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.INVOICED);
        assertThat(inventory.getAvailableQuantity()).isEqualTo(990);
        assertThat(invoiceRepository.findByOrderId(order.getId())).isPresent();
    }
}
