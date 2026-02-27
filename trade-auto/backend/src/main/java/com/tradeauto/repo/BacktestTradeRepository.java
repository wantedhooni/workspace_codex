package com.tradeauto.repo;

import com.tradeauto.model.BacktestTrade;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BacktestTradeRepository extends JpaRepository<BacktestTrade, Long> {
}
