package com.example.samplerecommend.repository;

import com.example.samplerecommend.domain.CustomerAction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 고객 행동 이력을 저장하고 조회한다.
 */
public interface CustomerActionRepository extends JpaRepository<CustomerAction, Long> {

    List<CustomerAction> findTop10ByCustomerIdOrderByActedAtDesc(String customerId);
}

