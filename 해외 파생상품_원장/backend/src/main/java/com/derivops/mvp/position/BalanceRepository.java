package com.derivops.mvp.position;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BalanceRepository extends JpaRepository<Balance, Long> {
    List<Balance> findByAccountIdAndTradingDate(Long accountId, LocalDate tradingDate);

    Balance findFirstByAccountIdOrderByTradingDateDesc(Long accountId);
}
