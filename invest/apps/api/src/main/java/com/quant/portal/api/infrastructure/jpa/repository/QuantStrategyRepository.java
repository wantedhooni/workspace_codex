package com.quant.portal.api.infrastructure.jpa.repository;

import com.quant.portal.api.infrastructure.jpa.repository.query.QuantStrategyQueryRepository;
import com.quant.portal.domain.quant.entity.QuantStrategy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuantStrategyRepository extends JpaRepository<QuantStrategy, Long>, QuantStrategyQueryRepository {
}
