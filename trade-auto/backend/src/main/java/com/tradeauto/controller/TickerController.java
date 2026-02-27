package com.tradeauto.controller;

import com.tradeauto.dto.ApiResponse;
import com.tradeauto.dto.TickerDTO;
import com.tradeauto.model.Ticker;
import com.tradeauto.repo.TickerRepository;
import com.tradeauto.service.SeedService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tickers")
@CrossOrigin
public class TickerController {
    private final TickerRepository tickerRepository;
    private final SeedService seedService;

    public TickerController(TickerRepository tickerRepository, SeedService seedService) {
        this.tickerRepository = tickerRepository;
        this.seedService = seedService;
    }

    @GetMapping
    public ApiResponse<List<TickerDTO>> list() {
        List<TickerDTO> result = new ArrayList<>();
        for (Ticker ticker : tickerRepository.findAll()) {
            TickerDTO dto = new TickerDTO();
            dto.id = ticker.getId();
            dto.symbol = ticker.getSymbol();
            dto.name = ticker.getName();
            dto.sector = ticker.getSector();
            dto.active = ticker.isActive();
            result.add(dto);
        }
        return ApiResponse.ok(result);
    }

    @PostMapping("/seed")
    public ApiResponse<String> seed() {
        int created = seedService.seedTickers();
        return ApiResponse.ok("seeded " + created);
    }

    @PostMapping("/{symbol}/toggle")
    public ApiResponse<TickerDTO> toggle(@PathVariable String symbol) {
        Ticker ticker = tickerRepository.findBySymbol(symbol).orElseThrow();
        ticker.setActive(!ticker.isActive());
        tickerRepository.save(ticker);
        TickerDTO dto = new TickerDTO();
        dto.id = ticker.getId();
        dto.symbol = ticker.getSymbol();
        dto.name = ticker.getName();
        dto.sector = ticker.getSector();
        dto.active = ticker.isActive();
        return ApiResponse.ok(dto);
    }
}
