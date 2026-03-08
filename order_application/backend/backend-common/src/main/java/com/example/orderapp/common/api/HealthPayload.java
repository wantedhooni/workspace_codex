package com.example.orderapp.common.api;

public record HealthPayload(
    String service,
    String status
) {
}
