package com.revy.mvpbanking.funding.domain;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FundingRequestRepository extends JpaRepository<FundingRequest, UUID> {
    List<FundingRequest> findAllByOrderByCreatedAtDesc();
    List<FundingRequest> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    Optional<FundingRequest> findByIdAndCustomerId(UUID id, UUID customerId);
    List<FundingRequest> findByCustomerIdAndRequestTypeAndCreatedAtBetween(UUID customerId, FundingRequestType requestType, Instant start, Instant end);
    Optional<FundingRequest> findByRequestNumber(String requestNumber);
}
