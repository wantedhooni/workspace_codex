package com.tradingmacro.risk;

import com.tradingmacro.portfolio.Portfolio;
import com.tradingmacro.trade.TradeRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class RiskCheckService {
    public void validateTrade(TradeRequest request, RiskPolicy policy, Portfolio portfolio) {
        if (policy == null || portfolio == null) {
            return;
        }

        BigDecimal notional = request.price().multiply(request.quantity());
        if (policy.getMaxPositionSize() != null && notional.compareTo(policy.getMaxPositionSize()) > 0) {
            throw new IllegalArgumentException("Risk limit exceeded: max position size");
        }

        if (policy.getMaxLeverage() != null && portfolio.getTotalValue().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal leverage = notional.divide(portfolio.getTotalValue(), 4, RoundingMode.HALF_UP);
            if (leverage.compareTo(policy.getMaxLeverage()) > 0) {
                throw new IllegalArgumentException("Risk limit exceeded: max leverage");
            }
        }
    }
}
