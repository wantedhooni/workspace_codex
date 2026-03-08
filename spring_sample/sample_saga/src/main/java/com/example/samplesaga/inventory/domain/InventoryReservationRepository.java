package com.example.samplesaga.inventory.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, String> {

    Optional<InventoryReservation> findByOrderId(String orderId);
}
