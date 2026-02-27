package com.quant.portal.api.infrastructure.jpa.repository;

import com.quant.portal.api.infrastructure.jpa.repository.query.HoldingQueryRepository;
import com.quant.portal.domain.portfolio.entity.Holding;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

public interface HoldingRepository extends JpaRepository<Holding, Long>, HoldingQueryRepository {

    @Override
    @EntityGraph(attributePaths = {"portfolio", "instrument"})
    Optional<Holding> findById(Long id);

    Optional<Holding> findByPortfolioIdAndInstrumentId(Long portfolioId, Long instrumentId);

    @EntityGraph(attributePaths = {"instrument"})
    List<Holding> findAllByPortfolioId(Long portfolioId);

    long countByPortfolioId(Long portfolioId);

    long countByInstrumentId(Long instrumentId);
}
