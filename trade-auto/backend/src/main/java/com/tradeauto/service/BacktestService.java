package com.tradeauto.service;

import com.tradeauto.dto.BacktestRunDTO;
import com.tradeauto.dto.BacktestTradeDTO;
import com.tradeauto.model.*;
import com.tradeauto.repo.BacktestRunRepository;
import com.tradeauto.repo.BacktestTradeRepository;
import com.tradeauto.repo.DailyBarRepository;
import com.tradeauto.repo.TickerRepository;
import com.tradeauto.util.IndicatorUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class BacktestService {
    private final TickerRepository tickerRepository;
    private final DailyBarRepository dailyBarRepository;
    private final BacktestRunRepository backtestRunRepository;
    private final BacktestTradeRepository backtestTradeRepository;

    private final StrategyConfigService strategyConfigService;

    public BacktestService(TickerRepository tickerRepository,
                           DailyBarRepository dailyBarRepository,
                           BacktestRunRepository backtestRunRepository,
                           BacktestTradeRepository backtestTradeRepository,
                           StrategyConfigService strategyConfigService) {
        this.tickerRepository = tickerRepository;
        this.dailyBarRepository = dailyBarRepository;
        this.backtestRunRepository = backtestRunRepository;
        this.backtestTradeRepository = backtestTradeRepository;
        this.strategyConfigService = strategyConfigService;
    }

    public BacktestRunDTO runBacktest(LocalDate start, LocalDate end, String strategyName) {
        BacktestRun run = new BacktestRun();
        run.setStrategyName(strategyName == null ? "Momentum-Score" : strategyName);
        run.setStartDate(start);
        run.setEndDate(end);

        List<BacktestTrade> allTrades = new ArrayList<>();
        double equity = 1.0;
        double peak = 1.0;
        double maxDrawdown = 0.0;
        int wins = 0;

        var config = strategyConfigService.get();
        for (Ticker ticker : tickerRepository.findAll()) {
            if (!ticker.isActive()) {
                continue;
            }
            List<DailyBar> bars = dailyBarRepository.findByTickerAndTradeDateBetweenOrderByTradeDateAsc(ticker, start, end);
            int trendDays = config.getTrendDays();
            int rsiDays = config.getRsiDays();
            int volatilityDays = config.getVolatilityDays();
            if (bars.size() < Math.max(trendDays, Math.max(rsiDays, volatilityDays)) + 2) {
                continue;
            }
            boolean inPosition = false;
            double entry = 0.0;
            double stop = 0.0;
            double target = 0.0;
            LocalDate entryDate = null;

            for (int i = Math.max(trendDays, rsiDays) + 1; i < bars.size(); i++) {
                DailyBar bar = bars.get(i);
                if (!inPosition) {
                    int score = scoreAtIndex(bars, i, config);
                    if (score >= 4) {
                        entry = bar.getClose();
                        if (entry < config.getMinPrice()) {
                            continue;
                        }
                        double atr = IndicatorUtil.atr(bars, i, rsiDays);
                        stop = entry * (1.0 - config.getStopLossPct());
                        if (!Double.isNaN(atr)) {
                            stop = Math.min(stop, entry - atr * 1.5);
                        }
                        double risk = entry - stop;
                        target = entry + risk * config.getRiskReward();
                        inPosition = true;
                        entryDate = bar.getTradeDate();
                    }
                } else {
                    double low = bar.getLow();
                    double high = bar.getHigh();
                    double exitPrice = bar.getClose();
                    String reason = "Time";
                    boolean exit = false;
                    if (low <= stop) {
                        exitPrice = stop;
                        reason = "Stop";
                        exit = true;
                    } else if (high >= target) {
                        exitPrice = target;
                        reason = "Target";
                        exit = true;
                    } else if (i - 1 >= 0 && i - 1 - momentumDays >= 0) {
                        int score = scoreAtIndex(bars, i, config);
                        if (score <= 1) {
                            exit = true;
                            reason = "Score";
                        }
                    }
                    if (exit) {
                        BacktestTrade trade = new BacktestTrade();
                        trade.setRun(run);
                        trade.setTicker(ticker);
                        trade.setEntryDate(entryDate);
                        trade.setExitDate(bar.getTradeDate());
                        trade.setEntryPrice(entry);
                        trade.setExitPrice(exitPrice);
                        double pnl = (exitPrice - entry) / entry;
                        trade.setPnlPct(pnl * 100.0);
                        trade.setExitReason(reason);
                        allTrades.add(trade);

                        equity = equity * (1.0 + pnl);
                        peak = Math.max(peak, equity);
                        double dd = (peak - equity) / peak;
                        maxDrawdown = Math.max(maxDrawdown, dd);
                        if (pnl > 0) {
                            wins++;
                        }
                        inPosition = false;
                        entry = 0.0;
                    }
                }
            }
        }

        run.setTrades(allTrades.size());
        run.setTotalReturnPct((equity - 1.0) * 100.0);
        run.setMaxDrawdownPct(maxDrawdown * 100.0);
        run.setWinRatePct(allTrades.isEmpty() ? 0.0 : (wins * 100.0 / allTrades.size()));
        run.getTradeList().addAll(allTrades);
        backtestRunRepository.save(run);
        backtestTradeRepository.saveAll(allTrades);

        return toDto(run);
    }

    public List<BacktestRunDTO> getRuns() {
        List<BacktestRunDTO> result = new ArrayList<>();
        for (BacktestRun run : backtestRunRepository.findAll()) {
            result.add(toDto(run));
        }
        return result;
    }

    public BacktestRunDTO getRun(Long id) {
        BacktestRun run = backtestRunRepository.findById(id).orElseThrow();
        return toDto(run);
    }

    private int scoreAtIndex(List<DailyBar> bars, int endIndex, com.tradeauto.model.StrategyConfig config) {
        DailyBar last = bars.get(endIndex);
        double avgVolume = bars.subList(Math.max(0, endIndex - 19), endIndex + 1)
                .stream().mapToLong(DailyBar::getVolume).average().orElse(0.0);
        if (avgVolume < config.getMinAvgVolume() || last.getClose() < config.getMinPrice()) {
            return -10;
        }
        int momentumDays = config.getMomentumDays();
        int trendDays = config.getTrendDays();
        int rsiDays = config.getRsiDays();
        int volatilityDays = config.getVolatilityDays();
        double sma20 = IndicatorUtil.sma(bars, endIndex, momentumDays);
        double sma60 = IndicatorUtil.sma(bars, endIndex, trendDays);
        double rsi = IndicatorUtil.rsi(bars, endIndex, rsiDays);
        double volatility = IndicatorUtil.volatility(bars, endIndex, volatilityDays);
        double momentum = 0.0;
        if (endIndex - momentumDays >= 0) {
            double past = bars.get(endIndex - momentumDays).getClose();
            momentum = (last.getClose() - past) / past;
        }
        boolean volumeSpike = last.getVolume() > avgVolume * config.getVolumeSpikeMultiplier();

        int score = 0;
        if (!Double.isNaN(sma60) && last.getClose() > sma60) {
            score += 2;
        } else if (!Double.isNaN(sma60)) {
            score -= 1;
        }
        if (!Double.isNaN(sma20) && last.getClose() > sma20) {
            score += 1;
        }
        if (momentum > 0.10) {
            score += 2;
        } else if (momentum > 0.05) {
            score += 1;
        } else if (momentum < -0.05) {
            score -= 1;
        }
        if (!Double.isNaN(rsi)) {
            if (rsi >= 50 && rsi <= 70) {
                score += 1;
            } else if (rsi > 80 || rsi < 30) {
                score -= 1;
            }
        }
        if (volumeSpike) {
            score += 1;
        }
        if (!Double.isNaN(volatility) && volatility > 0.05) {
            score -= 1;
        }
        return score;
    }

    private BacktestRunDTO toDto(BacktestRun run) {
        BacktestRunDTO dto = new BacktestRunDTO();
        dto.id = run.getId();
        dto.strategyName = run.getStrategyName();
        dto.startDate = run.getStartDate();
        dto.endDate = run.getEndDate();
        dto.totalReturnPct = run.getTotalReturnPct();
        dto.maxDrawdownPct = run.getMaxDrawdownPct();
        dto.winRatePct = run.getWinRatePct();
        dto.trades = run.getTrades();
        dto.createdAt = run.getCreatedAt();
        dto.tradeList = new ArrayList<>();
        for (BacktestTrade trade : run.getTradeList()) {
            BacktestTradeDTO t = new BacktestTradeDTO();
            t.id = trade.getId();
            t.symbol = trade.getTicker().getSymbol();
            t.entryDate = trade.getEntryDate();
            t.exitDate = trade.getExitDate();
            t.entryPrice = trade.getEntryPrice();
            t.exitPrice = trade.getExitPrice();
            t.pnlPct = trade.getPnlPct();
            t.exitReason = trade.getExitReason();
            dto.tradeList.add(t);
        }
        return dto;
    }
}
