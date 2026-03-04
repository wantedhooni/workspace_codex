package com.revy.mvpbanking.exchange.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeRequestRepository extends JpaRepository<ExchangeRequest, UUID> {
    List<ExchangeRequest> findAllByOrderByCreatedAtDesc();
    List<ExchangeRequest> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    Optional<ExchangeRequest> findByRequestNumber(String requestNumber);
}
