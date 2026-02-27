package com.tradingmacro.trade;

import com.tradingmacro.portfolio.Portfolio;
import com.tradingmacro.portfolio.PortfolioRepository;
import com.tradingmacro.risk.RiskPolicy;
import com.tradingmacro.risk.RiskPolicyRepository;
import com.tradingmacro.risk.RiskCheckService;
import com.tradingmacro.strategy.StrategyRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trades")
public class TradeController {
    private final TradeRepository repository;
    private final StrategyRepository strategyRepository;
    private final PortfolioRepository portfolioRepository;
    private final RiskPolicyRepository riskPolicyRepository;
    private final RiskCheckService riskCheckService;

    public TradeController(TradeRepository repository,
                           StrategyRepository strategyRepository,
                           PortfolioRepository portfolioRepository,
                           RiskPolicyRepository riskPolicyRepository,
                           RiskCheckService riskCheckService) {
        this.repository = repository;
        this.strategyRepository = strategyRepository;
        this.portfolioRepository = portfolioRepository;
        this.riskPolicyRepository = riskPolicyRepository;
        this.riskCheckService = riskCheckService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<Trade> list() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Trade get(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Trade not found"));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Trade create(@Valid @RequestBody TradeRequest request) {
        Trade trade = new Trade();
        applyRequest(trade, request);
        return repository.save(trade);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Trade update(@PathVariable Long id, @Valid @RequestBody TradeRequest request) {
        Trade trade = get(id);
        applyRequest(trade, request);
        return repository.save(trade);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }

    private void applyRequest(Trade trade, TradeRequest request) {
        trade.setSymbol(request.symbol());
        trade.setSide(request.side());
        trade.setQuantity(request.quantity());
        trade.setPrice(request.price());
        if (request.executedAt() != null) {
            trade.setExecutedAt(request.executedAt());
        }
        if (request.strategyId() != null) {
            trade.setStrategy(strategyRepository.findById(request.strategyId())
                .orElseThrow(() -> new IllegalArgumentException("Strategy not found")));
        } else {
            trade.setStrategy(null);
        }

        Portfolio portfolio = null;
        if (request.portfolioId() != null) {
            portfolio = portfolioRepository.findById(request.portfolioId())
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found"));
            trade.setPortfolio(portfolio);
        } else {
            trade.setPortfolio(null);
        }

        RiskPolicy policy = null;
        if (portfolio != null) {
            policy = riskPolicyRepository.findFirstByPortfolioId(portfolio.getId()).orElse(null);
        }
        riskCheckService.validateTrade(request, policy, portfolio);
    }
}
