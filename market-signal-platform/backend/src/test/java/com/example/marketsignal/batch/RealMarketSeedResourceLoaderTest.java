package com.example.marketsignal.batch;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RealMarketSeedResourceLoaderTest {

    @Test
    @DisplayName("실제 시장 시드 리소스를 역직렬화할 수 있다")
    void loadReturnsSeedData() {
        RealMarketSeedResourceLoader loader = new RealMarketSeedResourceLoader(
                new ObjectMapper().registerModule(new JavaTimeModule())
        );

        RealMarketSeedData seedData = loader.load();

        assertThat(seedData.snapshotDate()).isNotNull();
        assertThat(seedData.sources()).isNotEmpty();
        assertThat(seedData.defaultWatchlist()).isNotEmpty();
        assertThat(seedData.sectorSnapshots()).extracting(RealMarketSeedData.SectorSeed::sectorName)
                .contains("ENERGY", "SOFTWARE");
        assertThat(seedData.stockSnapshots()).extracting(RealMarketSeedData.StockSeed::ticker)
                .contains("XOM", "CVX", "NFLX");
    }
}
