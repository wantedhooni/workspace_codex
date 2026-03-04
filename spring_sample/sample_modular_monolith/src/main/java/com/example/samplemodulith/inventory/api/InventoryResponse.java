package com.example.samplemodulith.inventory.api;

public record InventoryResponse(
        String sku,
        int availableQuantity,
        int reservedQuantity
) {
}
