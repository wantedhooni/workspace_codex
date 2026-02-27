package com.tradeauto.repo;

import com.tradeauto.model.Signal;
import com.tradeauto.model.Ticker;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface SignalRepository extends JpaRepository<Signal, Long> {
    List<Signal> findBySignalDate(LocalDate signalDate);
    List<Signal> findByTickerOrderBySignalDateDesc(Ticker ticker);
}
