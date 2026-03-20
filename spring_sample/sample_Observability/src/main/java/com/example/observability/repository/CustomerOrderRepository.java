package com.example.observability.repository;

import com.example.observability.domain.CustomerOrder;
import com.example.observability.domain.OrderStatus;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * 주문 운영 데이터를 저장하고 조회한다.
 */
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findByTenantIdOrderByCreatedAtDesc(String tenantId);

    Optional<CustomerOrder> findByIdAndTenantId(Long id, String tenantId);

    long countByTenantIdAndStatus(String tenantId, OrderStatus status);

    @Query("""
            select coalesce(sum(o.amount), 0)
            from CustomerOrder o
            where o.tenantId = :tenantId
            """)
    BigDecimal sumAmountByTenantId(String tenantId);
}
