package com.example.securities.channel;

import java.time.LocalDateTime;

public class ChannelDtos {

    public record CreateApplicationRequest(String customerId, String productCode, String idempotencyKey) {
    }

    public record ApplicationResponse(
            String id,
            String customerId,
            String productCode,
            ChannelApplicationStatus status,
            String accountId,
            LocalDateTime createdAt
    ) {
    }
}
