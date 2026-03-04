package com.example.apigatewaywebflux.downstream;

import java.time.OffsetDateTime;

public record CustomerServiceResponse(
        String customerCode,
        String status,
        String segment,
        OffsetDateTime respondedAt
) {
}
