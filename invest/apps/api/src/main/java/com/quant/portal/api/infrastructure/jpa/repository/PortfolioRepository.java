package com.quant.portal.api.infrastructure.jpa.repository;

import com.quant.portal.api.infrastructure.jpa.repository.query.PortfolioQueryRepository;
import com.quant.portal.domain.portfolio.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long>, PortfolioQueryRepository {
}
