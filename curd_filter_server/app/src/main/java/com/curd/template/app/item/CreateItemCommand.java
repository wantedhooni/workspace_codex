package com.curd.template.app.item;

import com.curd.template.domain.item.ItemStatus;
import java.math.BigDecimal;

public record CreateItemCommand(
    String name,
    String description,
    BigDecimal price,
    ItemStatus status
) {
}
