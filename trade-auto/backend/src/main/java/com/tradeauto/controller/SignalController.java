package com.tradeauto.controller;

import com.tradeauto.dto.ApiResponse;
import com.tradeauto.dto.SignalDTO;
import com.tradeauto.service.SignalService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/signals")
@CrossOrigin
public class SignalController {
    private final SignalService signalService;

    public SignalController(SignalService signalService) {
        this.signalService = signalService;
    }

    @PostMapping("/generate")
    public ApiResponse<List<SignalDTO>> generate() {
        return ApiResponse.ok(signalService.generateLatestSignals());
    }

    @GetMapping("/latest")
    public ApiResponse<List<SignalDTO>> latest(@RequestParam(required = false) String date) {
        LocalDate target = date == null ? LocalDate.now() : LocalDate.parse(date);
        return ApiResponse.ok(signalService.getLatestSignals(target));
    }

    @GetMapping("/{symbol}")
    public ApiResponse<List<SignalDTO>> bySymbol(@PathVariable String symbol) {
        return ApiResponse.ok(signalService.getSignalsForSymbol(symbol));
    }
}
