package com.example.securities.external;

import java.time.LocalDateTime;

public class ExternalDtos {

    public record SendMessageRequest(String messageType, String payload) {
    }

    public record ExternalMessageResponse(
            String id,
            String messageType,
            String payload,
            ExternalMessageStatus status,
            int retryCount,
            String lastError,
            LocalDateTime createdAt
    ) {
    }
}
