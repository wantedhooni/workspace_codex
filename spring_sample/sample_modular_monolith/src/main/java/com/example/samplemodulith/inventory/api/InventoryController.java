package com.example.samplemodulith.inventory.api;

import com.example.samplemodulith.inventory.domain.InventoryItemRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryItemRepository inventoryItemRepository;

    public InventoryController(InventoryItemRepository inventoryItemRepository) {
        this.inventoryItemRepository = inventoryItemRepository;
    }

    @GetMapping("/{sku}")
    public InventoryResponse get(@PathVariable String sku) {
        var item = inventoryItemRepository.findById(sku)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "재고를 찾을 수 없습니다."));
        return new InventoryResponse(item.getSku(), item.getAvailableQuantity(), item.getReservedQuantity());
    }
}
