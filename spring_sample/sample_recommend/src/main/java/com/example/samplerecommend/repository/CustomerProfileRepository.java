package com.example.samplerecommend.repository;

import com.example.samplerecommend.domain.CustomerProfile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 고객 프로필을 조회한다.
 */
public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Long> {

    Optional<CustomerProfile> findByCustomerId(String customerId);
}

