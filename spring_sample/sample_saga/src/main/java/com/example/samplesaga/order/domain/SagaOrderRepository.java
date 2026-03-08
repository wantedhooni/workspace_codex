package com.example.samplesaga.order.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SagaOrderRepository extends JpaRepository<SagaOrder, String> {

    List<SagaOrder> findTop20ByOrderByCreatedAtDesc();
}
