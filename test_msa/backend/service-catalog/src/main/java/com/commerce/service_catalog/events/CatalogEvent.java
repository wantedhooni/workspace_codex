package com.commerce.service_catalog.events;

import java.time.Instant;

public record CatalogEvent(
    String eventId,
    String type,
    String sku,
    String name,
    Integer price,
    Instant occurredAt
) {}
