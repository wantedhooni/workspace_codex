package com.example.marketsignal.batch;

import com.example.marketsignal.config.AppProperties;
import com.example.marketsignal.macro.MacroSnapshot;
import com.example.marketsignal.macro.MacroSnapshotRepository;
import com.example.marketsignal.report.DailyReportRepository;
import com.example.marketsignal.sector.SectorSnapshot;
import com.example.marketsignal.sector.SectorSnapshotRepository;
import com.example.marketsignal.signal.SignalRepository;
import com.example.marketsignal.stock.StockSnapshot;
import com.example.marketsignal.stock.StockSnapshotRepository;
import com.example.marketsignal.user.User;
import com.example.marketsignal.user.UserRepository;
import com.example.marketsignal.user.UserRole;
import com.example.marketsignal.watchlist.Watchlist;
import com.example.marketsignal.watchlist.WatchlistRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 실제 시장 스냅샷 시드 데이터를 데이터베이스 구조에 맞게 적재한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RealMarketSeedService {

    private final AppProperties appProperties;
    private final RealMarketSeedResourceLoader realMarketSeedResourceLoader;
    private final UserRepository userRepository;
    private final WatchlistRepository watchlistRepository;
    private final MacroSnapshotRepository macroSnapshotRepository;
    private final SectorSnapshotRepository sectorSnapshotRepository;
    private final StockSnapshotRepository stockSnapshotRepository;
    private final SignalRepository signalRepository;
    private final DailyReportRepository dailyReportRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * 실제 시장 시드 JSON을 읽어 기본 계정, 관심 종목, 시장 스냅샷을 최신 기준으로 동기화한다.
     */
    @Transactional
    public RealMarketSeedData syncSeedData() {
        RealMarketSeedData seedData = realMarketSeedResourceLoader.load();
        User seedUser = ensureSeedUser();
        replaceDefaultWatchlist(seedUser, seedData.defaultWatchlist());
        replaceMarketSnapshotsIfNeeded(seedData);
        return seedData;
    }

    /**
     * 테스트용 기본 계정을 보장한다.
     */
    private User ensureSeedUser() {
        return userRepository.findByEmail("demo@marketsignal.dev")
                .orElseGet(() -> userRepository.save(User.builder()
                        .email("demo@marketsignal.dev")
                        .password(passwordEncoder.encode("Demo1234!"))
                        .name("Market Analyst")
                        .bio("실제 시장 스냅샷으로 시그널을 검증합니다.")
                        .role(UserRole.ROLE_USER)
                        .build()));
    }

    /**
     * 기본 계정의 관심 종목을 시드 기준으로 다시 구성한다.
     */
    private void replaceDefaultWatchlist(User seedUser, List<RealMarketSeedData.WatchlistSeed> watchlistSeeds) {
        watchlistRepository.deleteAll(watchlistRepository.findAllByUserIdOrderByCreatedAtDesc(seedUser.getId()));
        watchlistRepository.saveAll(watchlistSeeds.stream()
                .map(item -> Watchlist.builder()
                        .user(seedUser)
                        .ticker(item.ticker())
                        .companyName(item.companyName())
                        .build())
                .toList());
    }

    /**
     * 설정에 따라 시장 스냅샷과 리포트 데이터를 실제 시드 기준으로 교체한다.
     */
    private void replaceMarketSnapshotsIfNeeded(RealMarketSeedData seedData) {
        boolean snapshotAlreadyLoaded = macroSnapshotRepository.existsBySnapshotDate(seedData.snapshotDate())
                && sectorSnapshotRepository.existsBySnapshotDate(seedData.snapshotDate())
                && stockSnapshotRepository.existsBySnapshotDate(seedData.snapshotDate());

        if (snapshotAlreadyLoaded && !appProperties.seed().replaceMarketData()) {
            return;
        }

        signalRepository.deleteAllInBatch();
        dailyReportRepository.deleteAllInBatch();
        stockSnapshotRepository.deleteAllInBatch();
        sectorSnapshotRepository.deleteAllInBatch();
        macroSnapshotRepository.deleteAllInBatch();

        macroSnapshotRepository.save(MacroSnapshot.builder()
                .snapshotDate(seedData.snapshotDate())
                .tenYearYieldChange(seedData.macroSnapshot().tenYearYieldChange())
                .dxyChange(seedData.macroSnapshot().dxyChange())
                .oilChange(seedData.macroSnapshot().oilChange())
                .futuresChange(seedData.macroSnapshot().futuresChange())
                .build());

        sectorSnapshotRepository.saveAll(seedData.sectorSnapshots().stream()
                .map(item -> SectorSnapshot.builder()
                        .snapshotDate(seedData.snapshotDate())
                        .sectorName(item.sectorName())
                        .strengthScore(item.strengthScore())
                        .build())
                .toList());

        stockSnapshotRepository.saveAll(seedData.stockSnapshots().stream()
                .map(item -> StockSnapshot.builder()
                        .snapshotDate(seedData.snapshotDate())
                        .ticker(item.ticker())
                        .companyName(item.companyName())
                        .above20Dma(item.above20Dma())
                        .above50Dma(item.above50Dma())
                        .relativeStrengthStrong(item.relativeStrengthStrong())
                        .earningsReactionPositive(item.earningsReactionPositive())
                        .volumeSurge(item.volumeSurge())
                        .negativeNewsWeakPrice(item.negativeNewsWeakPrice())
                        .build())
                .toList());

        log.info("실제 시장 시드 데이터를 적재했습니다. snapshotDate={}, sources={}", seedData.snapshotDate(), seedData.sources());
    }
}
