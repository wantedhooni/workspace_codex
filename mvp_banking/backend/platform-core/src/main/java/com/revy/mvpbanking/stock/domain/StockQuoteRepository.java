package com.revy.mvpbanking.stock.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockQuoteRepository extends JpaRepository<StockQuote, java.util.UUID> {
    List<StockQuote> findAllByOrderByEffectiveAtDesc();
    Optional<StockQuote> findTopBySymbolIgnoreCaseAndMarketIgnoreCaseOrderByEffectiveAtDesc(String symbol, String market);
}
