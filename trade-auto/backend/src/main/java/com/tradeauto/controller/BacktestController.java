package com.tradeauto.controller;

import com.tradeauto.dto.ApiResponse;
import com.tradeauto.dto.BacktestRunDTO;
import com.tradeauto.dto.RunBacktestRequest;
import com.tradeauto.service.BacktestService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/backtest")
@CrossOrigin
public class BacktestController {
    private final BacktestService backtestService;

    public BacktestController(BacktestService backtestService) {
        this.backtestService = backtestService;
    }

    @PostMapping("/run")
    public ApiResponse<BacktestRunDTO> run(@RequestBody RunBacktestRequest request) {
        LocalDate start = request.startDate == null ? LocalDate.now().minusYears(2) : request.startDate;
        LocalDate end = request.endDate == null ? LocalDate.now() : request.endDate;
        return ApiResponse.ok(backtestService.runBacktest(start, end, request.strategyName));
    }

    @GetMapping("/runs")
    public ApiResponse<List<BacktestRunDTO>> runs() {
        return ApiResponse.ok(backtestService.getRuns());
    }

    @GetMapping("/runs/{id}")
    public ApiResponse<BacktestRunDTO> run(@PathVariable Long id) {
        return ApiResponse.ok(backtestService.getRun(id));
    }
}
