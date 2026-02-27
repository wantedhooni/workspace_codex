package com.tradeauto.controller;

import com.tradeauto.dto.ApiResponse;
import com.tradeauto.model.Ticker;
import com.tradeauto.repo.TickerRepository;
import com.tradeauto.service.MarketDataService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/marketdata")
@CrossOrigin
public class MarketDataController {
    private final MarketDataService marketDataService;
    private final TickerRepository tickerRepository;

    public MarketDataController(MarketDataService marketDataService, TickerRepository tickerRepository) {
        this.marketDataService = marketDataService;
        this.tickerRepository = tickerRepository;
    }

    @PostMapping("/refresh")
    public ApiResponse<String> refresh(@RequestParam(defaultValue = "365") int days) {
        marketDataService.refreshAllTickers(days);
        return ApiResponse.ok("refresh started");
    }

    @PostMapping("/refresh/{symbol}")
    public ApiResponse<String> refreshSymbol(@PathVariable String symbol,
                                @RequestParam(defaultValue = "365") int days) {
        Ticker ticker = tickerRepository.findBySymbol(symbol).orElseThrow();
        LocalDate end = LocalDate.now();
        marketDataService.refreshTicker(ticker, end.minusDays(days), end);
        return ApiResponse.ok("refreshed " + symbol);
    }
}
