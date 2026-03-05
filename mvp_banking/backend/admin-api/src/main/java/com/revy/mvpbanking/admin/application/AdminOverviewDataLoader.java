package com.revy.mvpbanking.admin.application;

import com.revy.mvpbanking.account.domain.AccountRepository;
import com.revy.mvpbanking.approval.domain.ApprovalRequestRepository;
import com.revy.mvpbanking.customer.domain.CustomerRepository;
import com.revy.mvpbanking.exchange.domain.ExchangeRequestRepository;
import com.revy.mvpbanking.fx.domain.FxRate;
import com.revy.mvpbanking.fx.domain.FxRateRepository;
import com.revy.mvpbanking.funding.domain.FundingRequestRepository;
import com.revy.mvpbanking.stock.domain.StockOrderRepository;
import com.revy.mvpbanking.stock.domain.StockQuote;
import com.revy.mvpbanking.stock.domain.StockQuoteRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
class AdminOverviewDataLoader {

    private final ApprovalRequestRepository approvalRequestRepository;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final FundingRequestRepository fundingRequestRepository;
    private final ExchangeRequestRepository exchangeRequestRepository;
    private final StockOrderRepository stockOrderRepository;
    private final FxRateRepository fxRateRepository;
    private final StockQuoteRepository stockQuoteRepository;

    AdminOverviewDataLoader(
            ApprovalRequestRepository approvalRequestRepository,
            CustomerRepository customerRepository,
            AccountRepository accountRepository,
            FundingRequestRepository fundingRequestRepository,
            ExchangeRequestRepository exchangeRequestRepository,
            StockOrderRepository stockOrderRepository,
            FxRateRepository fxRateRepository,
            StockQuoteRepository stockQuoteRepository
    ) {
        this.approvalRequestRepository = approvalRequestRepository;
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.fundingRequestRepository = fundingRequestRepository;
        this.exchangeRequestRepository = exchangeRequestRepository;
        this.stockOrderRepository = stockOrderRepository;
        this.fxRateRepository = fxRateRepository;
        this.stockQuoteRepository = stockQuoteRepository;
    }

    AdminOverviewDataset load() {
        return new AdminOverviewDataset(
                approvalRequestRepository.findAllByOrderByCreatedAtDesc(),
                customerRepository.findAllByOrderByCreatedAtDesc(),
                accountRepository.findAllByOrderByCreatedAtDesc(),
                fundingRequestRepository.findAllByOrderByCreatedAtDesc(),
                exchangeRequestRepository.findAllByOrderByCreatedAtDesc(),
                stockOrderRepository.findAllByOrderByCreatedAtDesc(),
                latestFxRates(fxRateRepository.findAllByOrderByEffectiveAtDesc()),
                latestStockQuotes(stockQuoteRepository.findAllByOrderByEffectiveAtDesc())
        );
    }

    private List<FxRate> latestFxRates(List<FxRate> rates) {
        Map<String, FxRate> latestByPair = new LinkedHashMap<>();
        for (FxRate rate : rates) {
            latestByPair.putIfAbsent(rate.getBaseCurrency() + "/" + rate.getQuoteCurrency(), rate);
        }
        return List.copyOf(latestByPair.values());
    }

    private List<StockQuote> latestStockQuotes(List<StockQuote> quotes) {
        Map<String, StockQuote> latestBySymbol = new LinkedHashMap<>();
        for (StockQuote quote : quotes) {
            latestBySymbol.putIfAbsent(quote.getSymbol() + "/" + quote.getMarket(), quote);
        }
        return List.copyOf(latestBySymbol.values());
    }
}
