package com.tradeauto.repo;

import com.tradeauto.model.BacktestRun;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BacktestRunRepository extends JpaRepository<BacktestRun, Long> {
}
