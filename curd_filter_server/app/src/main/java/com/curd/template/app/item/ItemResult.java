package com.curd.template.app.item;

import com.curd.template.domain.item.ItemStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record ItemResult(
    String id,
    String name,
    String description,
    BigDecimal price,
    ItemStatus status,
    Long version,
    Instant createdAt,
    Instant updatedAt
) {
}
