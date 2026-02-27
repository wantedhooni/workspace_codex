package com.quant.portal.api.application.service;

import com.quant.portal.api.infrastructure.jpa.repository.HoldingRepository;
import com.quant.portal.domain.portfolio.entity.Holding;
import com.quant.portal.domain.portfolio.entity.Portfolio;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PortfolioPerformanceService {

    private static final int MONEY_SCALE = 4;
    private static final int RATE_SCALE = 4;

    private final HoldingRepository holdingRepository;
    private final StooqEodPriceClient stooqEodPriceClient;
    private final boolean stooqEnabled;

    public PortfolioPerformanceService(
            HoldingRepository holdingRepository,
            StooqEodPriceClient stooqEodPriceClient,
            @Value("${app.market-data.stooq-enabled:true}") boolean stooqEnabled
    ) {
        this.holdingRepository = holdingRepository;
        this.stooqEodPriceClient = stooqEodPriceClient;
        this.stooqEnabled = stooqEnabled;
    }

    @Transactional(readOnly = true)
    public PortfolioPerformanceSnapshot calculate(Portfolio portfolio) {
        List<Holding> holdings = holdingRepository.findAllByPortfolioId(portfolio.getId());

        BigDecimal investedAmount = BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        BigDecimal marketValue = BigDecimal.ZERO.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
        LocalDate latestEodDate = null;
        int pricedHoldings = 0;

        for (Holding holding : holdings) {
            BigDecimal quantity = holding.getQuantity();
            BigDecimal averageCost = holding.getAverageCost();
            BigDecimal holdingInvested = normalizeMoney(quantity.multiply(averageCost));
            investedAmount = normalizeMoney(investedAmount.add(holdingInvested));

            Optional<StooqEodPriceClient.StooqEodQuote> quote = fetchLatestQuote(holding.getInstrument().getTicker());
            if (quote.isPresent()) {
                BigDecimal closePrice = quote.get().closePrice();
                marketValue = normalizeMoney(marketValue.add(normalizeMoney(quantity.multiply(closePrice))));
                pricedHoldings++;
                LocalDate quoteDate = quote.get().date();
                if (latestEodDate == null || latestEodDate.isBefore(quoteDate)) {
                    latestEodDate = quoteDate;
                }
            } else {
                marketValue = normalizeMoney(marketValue.add(holdingInvested));
            }
        }

        BigDecimal eodValuationAmount = normalizeMoney(portfolio.getCashBalance().add(marketValue));
        BigDecimal basisAmount = normalizeMoney(portfolio.getCashBalance().add(investedAmount));
        BigDecimal eodProfitLoss = normalizeMoney(eodValuationAmount.subtract(basisAmount));
        BigDecimal eodReturnRate = basisAmount.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO.setScale(RATE_SCALE, RoundingMode.HALF_UP)
                : eodProfitLoss.divide(basisAmount, RATE_SCALE, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(RATE_SCALE, RoundingMode.HALF_UP);

        return new PortfolioPerformanceSnapshot(
                investedAmount,
                marketValue,
                eodValuationAmount,
                eodProfitLoss,
                eodReturnRate,
                latestEodDate,
                pricedHoldings,
                holdings.size()
        );
    }

    private Optional<StooqEodPriceClient.StooqEodQuote> fetchLatestQuote(String ticker) {
        if (!stooqEnabled) {
            return Optional.empty();
        }
        return stooqEodPriceClient.fetchLatestQuote(ticker);
    }

    private static BigDecimal normalizeMoney(BigDecimal value) {
        return value.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }
}
