package com.example.marketsignal.batch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import java.util.List;

/**
 * 실제 시장 스냅샷 기반 시드 데이터 구조를 표현한다.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RealMarketSeedData(
        LocalDate snapshotDate,
        List<String> sources,
        List<WatchlistSeed> defaultWatchlist,
        MacroSeed macroSnapshot,
        List<SectorSeed> sectorSnapshots,
        List<StockSeed> stockSnapshots
) {

    /**
     * 기본 관심 종목 시드 항목이다.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record WatchlistSeed(
            String ticker,
            String companyName
    ) {
    }

    /**
     * 매크로 스냅샷 시드 항목이다.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MacroSeed(
            double tenYearYieldChange,
            double dxyChange,
            double oilChange,
            double futuresChange
    ) {
    }

    /**
     * 섹터 강도 시드 항목이다.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SectorSeed(
            String sectorName,
            double strengthScore
    ) {
    }

    /**
     * 종목 스냅샷 시드 항목이다.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StockSeed(
            String ticker,
            String companyName,
            boolean above20Dma,
            boolean above50Dma,
            boolean relativeStrengthStrong,
            boolean earningsReactionPositive,
            boolean volumeSurge,
            boolean negativeNewsWeakPrice
    ) {
    }
}
