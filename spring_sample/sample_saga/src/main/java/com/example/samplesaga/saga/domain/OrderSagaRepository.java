package com.example.samplesaga.saga.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderSagaRepository extends JpaRepository<OrderSaga, String> {

    Optional<OrderSaga> findByOrderId(String orderId);

    List<OrderSaga> findTop20ByOrderByCreatedAtDesc();
}
