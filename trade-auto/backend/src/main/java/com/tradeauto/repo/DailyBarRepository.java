package com.tradeauto.repo;

import com.tradeauto.model.DailyBar;
import com.tradeauto.model.Ticker;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyBarRepository extends JpaRepository<DailyBar, Long> {
    List<DailyBar> findByTickerOrderByTradeDateAsc(Ticker ticker);
    List<DailyBar> findByTickerAndTradeDateBetweenOrderByTradeDateAsc(Ticker ticker, LocalDate start, LocalDate end);
    Optional<DailyBar> findFirstByTickerOrderByTradeDateDesc(Ticker ticker);
}
