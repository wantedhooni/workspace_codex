package com.quant.portal.api.infrastructure.jpa.repository.query;

import com.quant.portal.api.application.query.QuantStrategySearchCondition;
import com.quant.portal.domain.quant.entity.QuantStrategy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuantStrategyQueryRepository {

    Page<QuantStrategy> search(QuantStrategySearchCondition condition, Pageable pageable);
}
