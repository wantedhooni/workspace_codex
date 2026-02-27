package com.quant.portal.api.infrastructure.jpa.repository.query;

import com.quant.portal.api.application.query.PortfolioSearchCondition;
import com.quant.portal.domain.portfolio.entity.Portfolio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PortfolioQueryRepository {

    Page<Portfolio> search(PortfolioSearchCondition condition, Pageable pageable);
}
