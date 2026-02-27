package com.tradeauto.controller;

import com.tradeauto.dto.ApiResponse;
import com.tradeauto.dto.StrategyConfigDTO;
import com.tradeauto.model.StrategyConfig;
import com.tradeauto.service.StrategyConfigService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/strategy")
@CrossOrigin
public class StrategyConfigController {
    private final StrategyConfigService service;

    public StrategyConfigController(StrategyConfigService service) {
        this.service = service;
    }

    @GetMapping
    public ApiResponse<StrategyConfigDTO> get() {
        return ApiResponse.ok(toDto(service.get()));
    }

    @PutMapping
    public ApiResponse<StrategyConfigDTO> update(@RequestBody StrategyConfigDTO dto) {
        StrategyConfig updated = service.update(fromDto(dto));
        return ApiResponse.ok(toDto(updated));
    }

    private StrategyConfigDTO toDto(StrategyConfig config) {
        StrategyConfigDTO dto = new StrategyConfigDTO();
        dto.riskReward = config.getRiskReward();
        dto.stopLossPct = config.getStopLossPct();
        dto.minAvgVolume = config.getMinAvgVolume();
        dto.minPrice = config.getMinPrice();
        dto.lookbackDays = config.getLookbackDays();
        dto.momentumDays = config.getMomentumDays();
        dto.trendDays = config.getTrendDays();
        dto.rsiDays = config.getRsiDays();
        dto.volatilityDays = config.getVolatilityDays();
        dto.volumeSpikeMultiplier = config.getVolumeSpikeMultiplier();
        return dto;
    }

    private StrategyConfig fromDto(StrategyConfigDTO dto) {
        StrategyConfig config = new StrategyConfig();
        config.setRiskReward(dto.riskReward);
        config.setStopLossPct(dto.stopLossPct);
        config.setMinAvgVolume(dto.minAvgVolume);
        config.setMinPrice(dto.minPrice);
        config.setLookbackDays(dto.lookbackDays);
        config.setMomentumDays(dto.momentumDays);
        config.setTrendDays(dto.trendDays);
        config.setRsiDays(dto.rsiDays);
        config.setVolatilityDays(dto.volatilityDays);
        config.setVolumeSpikeMultiplier(dto.volumeSpikeMultiplier);
        return config;
    }
}
