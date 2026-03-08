package com.example.samplesaga.payment.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, String> {

    Optional<PaymentRecord> findByOrderId(String orderId);
}
