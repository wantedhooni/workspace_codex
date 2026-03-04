package com.revy.mvpbanking.account.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findAllByOrderByCreatedAtDesc();
    List<Account> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
    Optional<Account> findByAccountNumber(String accountNumber);
}
