package com.example.marketsignal.signal;

import com.example.marketsignal.stock.StockSnapshot;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 종목 스냅샷에 점수 규칙을 적용한다.
 */
@Service
public class SignalScoringService {

    /**
     * 종목 규칙을 적용해 점수와 액션을 계산한다.
     */
    public SignalResult scoreStock(StockSnapshot stockSnapshot) {
        int score = 0;
        List<String> reasons = new ArrayList<>();

        if (stockSnapshot.isAbove20Dma()) {
            score += 1;
            reasons.add("20DMA 상회");
        }
        if (stockSnapshot.isAbove50Dma()) {
            score += 1;
            reasons.add("50DMA 상회");
        }
        if (stockSnapshot.isRelativeStrengthStrong()) {
            score += 2;
            reasons.add("상대 강도 우위");
        }
        if (stockSnapshot.isEarningsReactionPositive()) {
            score += 2;
            reasons.add("실적 반응 긍정");
        }
        if (stockSnapshot.isVolumeSurge()) {
            score += 1;
            reasons.add("거래량 급증");
        }
        if (stockSnapshot.isNegativeNewsWeakPrice()) {
            score -= 2;
            reasons.add("악재 후 가격 약세");
        }

        SignalAction action = score >= 5 ? SignalAction.BUY : score >= 3 ? SignalAction.WATCH : SignalAction.AVOID;
        return new SignalResult(stockSnapshot.getTicker(), score, action, String.join(", ", reasons));
    }
}
