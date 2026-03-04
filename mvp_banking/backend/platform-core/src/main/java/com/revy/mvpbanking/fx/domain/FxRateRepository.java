package com.revy.mvpbanking.fx.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FxRateRepository extends JpaRepository<FxRate, UUID> {
    List<FxRate> findAllByOrderByEffectiveAtDesc();
    Optional<FxRate> findTopByBaseCurrencyAndQuoteCurrencyOrderByEffectiveAtDesc(String baseCurrency, String quoteCurrency);
}
