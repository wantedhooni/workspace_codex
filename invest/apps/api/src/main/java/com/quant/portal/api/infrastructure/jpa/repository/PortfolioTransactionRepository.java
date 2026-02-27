package com.quant.portal.api.infrastructure.jpa.repository;

import com.quant.portal.api.infrastructure.jpa.repository.query.PortfolioTransactionQueryRepository;
import com.quant.portal.domain.portfolio.entity.PortfolioTransaction;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioTransactionRepository
        extends JpaRepository<PortfolioTransaction, Long>, PortfolioTransactionQueryRepository {

    long countByPortfolioId(Long portfolioId);

    long countByInstrumentId(Long instrumentId);

    Optional<PortfolioTransaction> findTopByPortfolioIdOrderByTradeDateDescIdDesc(Long portfolioId);
}
