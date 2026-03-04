package com.example.samplemodulith.billing.api;

import com.example.samplemodulith.billing.domain.InvoiceStatus;
import java.math.BigDecimal;

public record InvoiceResponse(
        Long invoiceId,
        String orderId,
        BigDecimal amount,
        InvoiceStatus status
) {
}
