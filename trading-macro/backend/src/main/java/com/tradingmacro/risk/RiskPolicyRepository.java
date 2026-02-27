package com.tradingmacro.risk;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RiskPolicyRepository extends JpaRepository<RiskPolicy, Long> {
    Optional<RiskPolicy> findFirstByPortfolioId(Long portfolioId);
}
