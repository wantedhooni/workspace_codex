package com.example.samplemodulith.billing.api;

import com.example.samplemodulith.billing.domain.InvoiceRepository;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;

    public InvoiceController(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @GetMapping
    public List<InvoiceResponse> all() {
        return invoiceRepository.findAll().stream()
                .map(invoice -> new InvoiceResponse(invoice.getId(), invoice.getOrderId(), invoice.getAmount(), invoice.getStatus()))
                .toList();
    }
}
