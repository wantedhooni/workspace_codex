package com.quant.portal.api.infrastructure.jpa.repository.query;

import com.quant.portal.api.application.query.TransactionSearchCondition;
import com.quant.portal.domain.portfolio.entity.PortfolioTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PortfolioTransactionQueryRepository {

    Page<PortfolioTransaction> search(TransactionSearchCondition condition, Pageable pageable);
}
