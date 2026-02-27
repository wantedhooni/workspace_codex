package com.tradeauto.service;

import com.tradeauto.model.StrategyConfig;
import com.tradeauto.repo.StrategyConfigRepository;
import org.springframework.stereotype.Service;

@Service
public class StrategyConfigService {
    private final StrategyConfigRepository repository;

    public StrategyConfigService(StrategyConfigRepository repository) {
        this.repository = repository;
    }

    public StrategyConfig get() {
        return repository.findById(1L).orElseGet(() -> repository.save(new StrategyConfig()));
    }

    public StrategyConfig update(StrategyConfig incoming) {
        StrategyConfig existing = get();
        existing.setRiskReward(incoming.getRiskReward());
        existing.setStopLossPct(incoming.getStopLossPct());
        existing.setMinAvgVolume(incoming.getMinAvgVolume());
        existing.setMinPrice(incoming.getMinPrice());
        existing.setLookbackDays(incoming.getLookbackDays());
        existing.setMomentumDays(incoming.getMomentumDays());
        existing.setTrendDays(incoming.getTrendDays());
        existing.setRsiDays(incoming.getRsiDays());
        existing.setVolatilityDays(incoming.getVolatilityDays());
        existing.setVolumeSpikeMultiplier(incoming.getVolumeSpikeMultiplier());
        return repository.save(existing);
    }
}
