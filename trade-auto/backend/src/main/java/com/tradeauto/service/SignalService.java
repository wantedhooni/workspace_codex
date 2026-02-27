package com.tradeauto.service;

import com.tradeauto.dto.SignalDTO;
import com.tradeauto.model.DailyBar;
import com.tradeauto.model.Signal;
import com.tradeauto.model.SignalAction;
import com.tradeauto.model.Ticker;
import com.tradeauto.repo.DailyBarRepository;
import com.tradeauto.repo.SignalRepository;
import com.tradeauto.repo.TickerRepository;
import com.tradeauto.util.IndicatorUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class SignalService {
    private final TickerRepository tickerRepository;
    private final DailyBarRepository dailyBarRepository;
    private final SignalRepository signalRepository;

    private final StrategyConfigService strategyConfigService;

    public SignalService(TickerRepository tickerRepository,
                         DailyBarRepository dailyBarRepository,
                         SignalRepository signalRepository,
                         StrategyConfigService strategyConfigService) {
        this.tickerRepository = tickerRepository;
        this.dailyBarRepository = dailyBarRepository;
        this.signalRepository = signalRepository;
        this.strategyConfigService = strategyConfigService;
    }

    public List<SignalDTO> generateLatestSignals() {
        LocalDate today = LocalDate.now();
        List<SignalDTO> result = new ArrayList<>();
        var config = strategyConfigService.get();
        for (Ticker ticker : tickerRepository.findAll()) {
            if (!ticker.isActive()) {
                continue;
            }
            List<DailyBar> bars = dailyBarRepository.findByTickerOrderByTradeDateAsc(ticker);
            int trendDays = config.getTrendDays();
            int rsiDays = config.getRsiDays();
            int volatilityDays = config.getVolatilityDays();
            if (bars.size() < Math.max(trendDays, Math.max(rsiDays, volatilityDays)) + 2) {
                continue;
            }
            int endIndex = bars.size() - 1;
            DailyBar last = bars.get(endIndex);
            if (last.getClose() < config.getMinPrice()) {
                continue;
            }
            double avgVolume = bars.subList(Math.max(0, endIndex - 19), endIndex + 1)
                    .stream().mapToLong(DailyBar::getVolume).average().orElse(0.0);
            if (avgVolume < config.getMinAvgVolume()) {
                continue;
            }

            int momentumDays = config.getMomentumDays();
            double sma20 = IndicatorUtil.sma(bars, endIndex, momentumDays);
            double sma60 = IndicatorUtil.sma(bars, endIndex, trendDays);
            double rsi = IndicatorUtil.rsi(bars, endIndex, rsiDays);
            double volatility = IndicatorUtil.volatility(bars, endIndex, volatilityDays);
            double atr = IndicatorUtil.atr(bars, endIndex, rsiDays);

            double momentum = 0.0;
            if (endIndex - momentumDays >= 0) {
                double past = bars.get(endIndex - momentumDays).getClose();
                momentum = (last.getClose() - past) / past;
            }

            boolean volumeSpike = last.getVolume() > avgVolume * config.getVolumeSpikeMultiplier();
            int score = 0;
            StringBuilder rationale = new StringBuilder();

            if (!Double.isNaN(sma60) && last.getClose() > sma60) {
                score += 2;
                rationale.append("Above 60D SMA. ");
            } else if (!Double.isNaN(sma60)) {
                score -= 1;
                rationale.append("Below 60D SMA. ");
            }

            if (!Double.isNaN(sma20) && last.getClose() > sma20) {
                score += 1;
                rationale.append("Above 20D SMA. ");
            }

            if (momentum > 0.10) {
                score += 2;
                rationale.append("Momentum > 10%. ");
            } else if (momentum > 0.05) {
                score += 1;
                rationale.append("Momentum > 5%. ");
            } else if (momentum < -0.05) {
                score -= 1;
                rationale.append("Momentum weak. ");
            }

            if (!Double.isNaN(rsi)) {
                if (rsi >= 50 && rsi <= 70) {
                    score += 1;
                    rationale.append("RSI healthy. ");
                } else if (rsi > 80 || rsi < 30) {
                    score -= 1;
                    rationale.append("RSI extreme. ");
                }
            }

            if (volumeSpike) {
                score += 1;
                rationale.append("Volume spike. ");
            }

            if (!Double.isNaN(volatility) && volatility > 0.05) {
                score -= 1;
                rationale.append("High volatility. ");
            }

            SignalAction action = SignalAction.HOLD;
            if (score >= 4) {
                action = SignalAction.BUY;
            } else if (score <= 1 && (!Double.isNaN(sma60) && last.getClose() < sma60)) {
                action = SignalAction.SELL;
            }

            double entry = last.getClose();
            double stopLoss = entry * (1.0 - config.getStopLossPct());
            if (!Double.isNaN(atr)) {
                stopLoss = Math.min(stopLoss, entry - atr * 1.5);
            }
            double risk = entry - stopLoss;
            double takeProfit = entry + risk * config.getRiskReward();

            Signal signal = new Signal();
            signal.setTicker(ticker);
            signal.setSignalDate(today);
            signal.setAction(action);
            signal.setScore(score);
            signal.setEntryPrice(entry);
            signal.setStopLoss(stopLoss);
            signal.setTakeProfit(takeProfit);
            signal.setRationale(rationale.toString().trim());
            signalRepository.save(signal);

            result.add(toDto(signal));
        }
        return result;
    }

    public List<SignalDTO> getLatestSignals(LocalDate date) {
        List<SignalDTO> result = new ArrayList<>();
        for (Signal signal : signalRepository.findBySignalDate(date)) {
            result.add(toDto(signal));
        }
        return result;
    }

    public List<SignalDTO> getSignalsForSymbol(String symbol) {
        Ticker ticker = tickerRepository.findBySymbol(symbol).orElseThrow();
        List<SignalDTO> result = new ArrayList<>();
        for (Signal signal : signalRepository.findByTickerOrderBySignalDateDesc(ticker)) {
            result.add(toDto(signal));
        }
        return result;
    }

    private SignalDTO toDto(Signal signal) {
        SignalDTO dto = new SignalDTO();
        dto.id = signal.getId();
        dto.symbol = signal.getTicker().getSymbol();
        dto.name = signal.getTicker().getName();
        dto.signalDate = signal.getSignalDate();
        dto.action = signal.getAction();
        dto.score = signal.getScore();
        dto.entryPrice = signal.getEntryPrice();
        dto.stopLoss = signal.getStopLoss();
        dto.takeProfit = signal.getTakeProfit();
        dto.rationale = signal.getRationale();
        return dto;
    }
}
