package com.quant.portal.api.infrastructure.jpa.repository;

import com.quant.portal.domain.quant.entity.QuantSignalExecution;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuantSignalExecutionRepository extends JpaRepository<QuantSignalExecution, Long> {

    boolean existsBySignal_Id(Long signalId);

    @EntityGraph(attributePaths = {"transaction"})
    Optional<QuantSignalExecution> findBySignal_Id(Long signalId);
}
