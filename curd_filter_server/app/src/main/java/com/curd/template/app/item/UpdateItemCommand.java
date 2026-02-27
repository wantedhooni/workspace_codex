package com.curd.template.app.item;

import com.curd.template.domain.item.ItemStatus;
import java.math.BigDecimal;

public record UpdateItemCommand(
    String name,
    String description,
    BigDecimal price,
    ItemStatus status
) {
}
