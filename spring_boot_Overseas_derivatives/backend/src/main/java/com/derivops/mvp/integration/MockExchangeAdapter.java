package com.derivops.mvp.integration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MockExchangeAdapter implements ExchangeAdapter {

    @Override
    public List<ExchangePositionSnapshot> fetchPositions(LocalDate tradingDate, String accountNo) {
        return List.of(
                new ExchangePositionSnapshot("ESM6", new BigDecimal("2"), new BigDecimal("5200.25")),
                new ExchangePositionSnapshot("NQM6", new BigDecimal("1"), new BigDecimal("18300.50"))
        );
    }

    @Override
    public MarginSnapshot fetchMargins(LocalDate tradingDate, String accountNo) {
        return new MarginSnapshot(
                new BigDecimal("120000.0000"),
                new BigDecimal("90000.0000"),
                new BigDecimal("35000.0000")
        );
    }
}
