package com.example.sampleeventdriven.order.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    Optional<CustomerOrder> findByIdempotencyKey(String idempotencyKey);
}
