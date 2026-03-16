package com.example.marketsignal.macro;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 매크로 스냅샷으로 시장 레짐을 판별한다.
 */
@Service
@RequiredArgsConstructor
public class MarketRegimeEngine {

    /**
     * 매크로 규칙을 적용해 시장 레짐을 계산한다.
     */
    public MarketRegime determine(MacroSnapshot snapshot) {
        if (snapshot.getTenYearYieldChange() < 0 && snapshot.getDxyChange() < 0) {
            return MarketRegime.GROWTH;
        }
        if (snapshot.getOilChange() >= 3.0) {
            return MarketRegime.ENERGY;
        }
        if (snapshot.getFuturesChange() <= -1.5) {
            return MarketRegime.RISK_OFF;
        }
        return MarketRegime.NEUTRAL;
    }
}
