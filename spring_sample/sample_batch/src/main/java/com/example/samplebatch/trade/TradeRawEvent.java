package com.example.samplebatch.trade;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TradeRawEvent(
        long id,
        String accountNo,
        String instrumentCode,
        BigDecimal quantity,
        BigDecimal price,
        String market,
        LocalDateTime executedAt
) {
}
