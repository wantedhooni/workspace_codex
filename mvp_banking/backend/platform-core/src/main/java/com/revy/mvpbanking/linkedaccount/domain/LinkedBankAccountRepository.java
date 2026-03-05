package com.revy.mvpbanking.linkedaccount.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LinkedBankAccountRepository extends JpaRepository<LinkedBankAccount, UUID> {
    List<LinkedBankAccount> findAllByOrderByCreatedAtDesc();
    List<LinkedBankAccount> findByCustomerIdOrderByPrimaryWithdrawalDescCreatedAtDesc(UUID customerId);
    Optional<LinkedBankAccount> findByCustomerIdAndAccountNumber(UUID customerId, String accountNumber);
}
