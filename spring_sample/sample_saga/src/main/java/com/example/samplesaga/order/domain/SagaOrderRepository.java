package com.example.samplesaga.order.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SagaOrderRepository extends JpaRepository<SagaOrder, String> {
}
