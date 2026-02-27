package com.quant.portal.api.infrastructure.jpa.repository;

import com.quant.portal.api.infrastructure.jpa.repository.query.InstrumentQueryRepository;
import com.quant.portal.domain.portfolio.entity.Instrument;
import com.quant.portal.domain.portfolio.enums.MarketCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InstrumentRepository extends JpaRepository<Instrument, Long>, InstrumentQueryRepository {

    Optional<Instrument> findByMarketCodeAndTicker(MarketCode marketCode, String ticker);
}
