package com.derivops.mvp.position;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MarginRepository extends JpaRepository<Margin, Long> {
    Margin findFirstByAccountIdOrderByTradingDateDesc(Long accountId);
}
