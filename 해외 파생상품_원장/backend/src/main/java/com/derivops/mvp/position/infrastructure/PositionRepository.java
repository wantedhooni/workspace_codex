package com.derivops.mvp.position.infrastructure;
import com.derivops.mvp.position.*;


import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PositionRepository extends JpaRepository<Position, Long> {
    List<Position> findByAccountIdAndTradingDate(Long accountId, LocalDate tradingDate);

    Position findFirstByAccountIdOrderByTradingDateDesc(Long accountId);
}
