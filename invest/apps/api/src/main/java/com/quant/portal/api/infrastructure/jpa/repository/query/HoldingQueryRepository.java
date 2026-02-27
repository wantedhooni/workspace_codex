package com.quant.portal.api.infrastructure.jpa.repository.query;

import com.quant.portal.api.application.query.HoldingSearchCondition;
import com.quant.portal.domain.portfolio.entity.Holding;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HoldingQueryRepository {

    Page<Holding> search(HoldingSearchCondition condition, Pageable pageable);
}
