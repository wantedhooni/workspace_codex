package com.derivops.mvp.integration;

import java.time.LocalDate;
import java.util.List;

public interface ExchangeAdapter {
    List<ExchangePositionSnapshot> fetchPositions(LocalDate tradingDate, String accountNo);

    MarginSnapshot fetchMargins(LocalDate tradingDate, String accountNo);
}
