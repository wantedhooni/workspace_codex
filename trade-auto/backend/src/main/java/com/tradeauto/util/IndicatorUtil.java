package com.tradeauto.util;

import com.tradeauto.model.DailyBar;
import java.util.ArrayList;
import java.util.List;

public class IndicatorUtil {
    private IndicatorUtil() {}

    public static double sma(List<DailyBar> bars, int endIndex, int window) {
        if (endIndex + 1 < window) {
            return Double.NaN;
        }
        double sum = 0.0;
        for (int i = endIndex; i > endIndex - window; i--) {
            sum += bars.get(i).getClose();
        }
        return sum / window;
    }

    public static double rsi(List<DailyBar> bars, int endIndex, int window) {
        if (endIndex < window) {
            return Double.NaN;
        }
        double gains = 0.0;
        double losses = 0.0;
        for (int i = endIndex - window + 1; i <= endIndex; i++) {
            double change = bars.get(i).getClose() - bars.get(i - 1).getClose();
            if (change >= 0) {
                gains += change;
            } else {
                losses -= change;
            }
        }
        if (losses == 0) {
            return 100.0;
        }
        double rs = gains / losses;
        return 100.0 - (100.0 / (1.0 + rs));
    }

    public static double volatility(List<DailyBar> bars, int endIndex, int window) {
        if (endIndex + 1 < window) {
            return Double.NaN;
        }
        List<Double> returns = new ArrayList<>();
        for (int i = endIndex - window + 1; i <= endIndex; i++) {
            double prev = bars.get(i - 1).getClose();
            double curr = bars.get(i).getClose();
            returns.add((curr - prev) / prev);
        }
        double mean = returns.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = returns.stream().mapToDouble(r -> Math.pow(r - mean, 2)).average().orElse(0.0);
        return Math.sqrt(variance);
    }

    public static double atr(List<DailyBar> bars, int endIndex, int window) {
        if (endIndex < window) {
            return Double.NaN;
        }
        double sum = 0.0;
        for (int i = endIndex - window + 1; i <= endIndex; i++) {
            DailyBar curr = bars.get(i);
            DailyBar prev = bars.get(i - 1);
            double tr1 = curr.getHigh() - curr.getLow();
            double tr2 = Math.abs(curr.getHigh() - prev.getClose());
            double tr3 = Math.abs(curr.getLow() - prev.getClose());
            double tr = Math.max(tr1, Math.max(tr2, tr3));
            sum += tr;
        }
        return sum / window;
    }
}
