package com.commerce.service_notification.events;

import java.time.Instant;

public record OrderEvent(
    String eventId,
    String type,
    Long orderId,
    Long customerId,
    Integer totalAmount,
    String status,
    Instant occurredAt
) {}
