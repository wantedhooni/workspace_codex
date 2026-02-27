package com.quant.portal.api.infrastructure.jpa.repository;

import com.quant.portal.api.infrastructure.jpa.repository.query.QuantSignalQueryRepository;
import com.quant.portal.domain.quant.entity.QuantSignal;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuantSignalRepository extends JpaRepository<QuantSignal, Long>, QuantSignalQueryRepository {

    @Override
    @EntityGraph(attributePaths = {"strategy", "instrument"})
    Optional<QuantSignal> findById(Long id);

    long countByStrategyId(Long strategyId);
}
