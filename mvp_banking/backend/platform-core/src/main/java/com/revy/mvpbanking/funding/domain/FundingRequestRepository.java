package com.revy.mvpbanking.funding.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FundingRequestRepository extends JpaRepository<FundingRequest, UUID> {
    List<FundingRequest> findAllByOrderByCreatedAtDesc();
    List<FundingRequest> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    Optional<FundingRequest> findByRequestNumber(String requestNumber);
}
