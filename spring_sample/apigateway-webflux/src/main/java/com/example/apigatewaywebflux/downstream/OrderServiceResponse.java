package com.example.apigatewaywebflux.downstream;

import java.time.OffsetDateTime;

public record OrderServiceResponse(
        String orderNumber,
        String status,
        int totalAmount,
        OffsetDateTime respondedAt
) {
}
