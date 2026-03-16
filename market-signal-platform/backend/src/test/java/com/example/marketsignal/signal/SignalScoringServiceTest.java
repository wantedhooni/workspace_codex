package com.example.marketsignal.signal;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.marketsignal.stock.StockSnapshot;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SignalScoringServiceTest {

    private final SignalScoringService signalScoringService = new SignalScoringService();

    @Test
    @DisplayName("강한 조건을 만족하는 종목은 BUY 시그널을 반환한다")
    void scoreStockReturnsBuySignal() {
        StockSnapshot snapshot = StockSnapshot.builder()
                .snapshotDate(LocalDate.now())
                .ticker("NVDA")
                .companyName("NVIDIA")
                .above20Dma(true)
                .above50Dma(true)
                .relativeStrengthStrong(true)
                .earningsReactionPositive(true)
                .volumeSurge(true)
                .negativeNewsWeakPrice(false)
                .build();

        SignalResult result = signalScoringService.scoreStock(snapshot);

        assertThat(result.score()).isEqualTo(7);
        assertThat(result.action()).isEqualTo(SignalAction.BUY);
    }

    @Test
    @DisplayName("약한 조건을 가진 종목은 AVOID 시그널을 반환한다")
    void scoreStockReturnsAvoidSignal() {
        StockSnapshot snapshot = StockSnapshot.builder()
                .snapshotDate(LocalDate.now())
                .ticker("TSLA")
                .companyName("Tesla")
                .above20Dma(false)
                .above50Dma(false)
                .relativeStrengthStrong(false)
                .earningsReactionPositive(false)
                .volumeSurge(true)
                .negativeNewsWeakPrice(true)
                .build();

        SignalResult result = signalScoringService.scoreStock(snapshot);

        assertThat(result.score()).isEqualTo(-1);
        assertThat(result.action()).isEqualTo(SignalAction.AVOID);
    }
}
