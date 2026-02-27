package com.curd.template.api.item;

import com.curd.template.domain.item.ItemStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record UpdateItemRequest(
    @NotBlank @Size(max = 120) String name,
    @Size(max = 1000) String description,
    @NotNull @DecimalMin("0.0") BigDecimal price,
    @NotNull ItemStatus status
) {
}
