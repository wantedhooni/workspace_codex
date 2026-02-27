package com.curd.template.api.item;

import com.curd.template.domain.item.ItemStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record PatchItemRequest(
    @Size(min = 1, max = 120) String name,
    @Size(max = 1000) String description,
    @DecimalMin("0.0") BigDecimal price,
    ItemStatus status
) {
}
