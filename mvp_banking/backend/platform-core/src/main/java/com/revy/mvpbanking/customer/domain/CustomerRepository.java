package com.revy.mvpbanking.customer.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByEndUserId(UUID endUserId);
    Optional<Customer> findByCustomerNumber(String customerNumber);
    List<Customer> findAllByOrderByCreatedAtDesc();
}
