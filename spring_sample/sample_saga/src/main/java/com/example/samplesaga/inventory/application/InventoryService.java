package com.example.samplesaga.inventory.application;

import com.example.samplesaga.inventory.domain.InventoryReservation;
import com.example.samplesaga.inventory.domain.InventoryReservationRepository;
import com.example.samplesaga.order.domain.SagaOrder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

    private final InventoryReservationRepository inventoryReservationRepository;

    public InventoryService(InventoryReservationRepository inventoryReservationRepository) {
        this.inventoryReservationRepository = inventoryReservationRepository;
    }

    @Transactional(noRollbackFor = IllegalStateException.class)
    public void reserveInventory(SagaOrder order) {
        if ("LIMITED-STOCK".equalsIgnoreCase(order.getProductCode())) {
            throw new IllegalStateException("재고 예약에 실패했습니다.");
        }

        inventoryReservationRepository.save(new InventoryReservation(order.getId(), order.getProductCode(), order.getQuantity()));
    }
}
