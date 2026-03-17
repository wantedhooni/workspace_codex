package com.example.marketsignal.watchlist;

import com.example.marketsignal.common.BusinessException;
import com.example.marketsignal.signal.SignalResult;
import com.example.marketsignal.signal.SignalScoringService;
import com.example.marketsignal.stock.StockSnapshot;
import com.example.marketsignal.stock.StockSnapshotRepository;
import com.example.marketsignal.user.CurrentUser;
import com.example.marketsignal.user.User;
import com.example.marketsignal.user.UserService;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 관심 종목 등록 및 조회 기능을 처리한다.
 */
@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final UserService userService;
    private final StockSnapshotRepository stockSnapshotRepository;
    private final SignalScoringService signalScoringService;

    /**
     * 관심 종목을 신규 등록한다.
     */
    @Transactional
    public WatchlistResponse create(CurrentUser currentUser, WatchlistCreateRequest request) {
        if (watchlistRepository.existsByUserIdAndTickerIgnoreCase(currentUser.id(), request.ticker())) {
            throw new BusinessException(HttpStatus.CONFLICT, "이미 등록된 관심 종목입니다.");
        }
        User user = userService.findUser(currentUser.id());
        Watchlist watchlist = Watchlist.builder()
                .user(user)
                .ticker(request.ticker().toUpperCase())
                .companyName(request.companyName())
                .build();
        return WatchlistResponse.from(watchlistRepository.save(watchlist));
    }

    /**
     * 현재 사용자의 관심 종목 목록을 조회한다.
     */
    @Transactional(readOnly = true)
    public List<WatchlistResponse> getAll(CurrentUser currentUser, String query) {
        String normalizedQuery = query == null ? "" : query.trim();
        List<Watchlist> watchlists = normalizedQuery.isBlank()
                ? watchlistRepository.findAllByUserIdOrderByCreatedAtDesc(currentUser.id())
                : watchlistRepository.searchByUserIdAndKeywordOrderByCreatedAtDesc(currentUser.id(), normalizedQuery);
        return watchlists.stream()
                .map(WatchlistResponse::from)
                .toList();
    }

    /**
     * 현재 사용자의 관심 종목에 최신 시그널 커버리지를 결합해 조회한다.
     */
    @Transactional(readOnly = true)
    public List<WatchlistCoverageResponse> getCoverage(CurrentUser currentUser, String query) {
        String normalizedQuery = query == null ? "" : query.trim();
        List<Watchlist> watchlists = normalizedQuery.isBlank()
                ? watchlistRepository.findAllByUserIdOrderByCreatedAtDesc(currentUser.id())
                : watchlistRepository.searchByUserIdAndKeywordOrderByCreatedAtDesc(currentUser.id(), normalizedQuery);

        if (watchlists.isEmpty()) {
            return List.of();
        }

        LocalDate latestSnapshotDate = stockSnapshotRepository.findTopByOrderBySnapshotDateDesc()
                .map(StockSnapshot::getSnapshotDate)
                .orElse(null);

        Map<String, StockSnapshot> snapshotsByTicker = latestSnapshotDate == null
                ? Map.of()
                : stockSnapshotRepository.findAllBySnapshotDateAndTickerIn(
                                latestSnapshotDate,
                                watchlists.stream().map(Watchlist::getTicker).toList()
                        ).stream()
                        .collect(java.util.stream.Collectors.toMap(StockSnapshot::getTicker, Function.identity()));

        return watchlists.stream()
                .map(watchlist -> {
                    StockSnapshot snapshot = snapshotsByTicker.get(watchlist.getTicker());
                    SignalResult result = snapshot == null ? null : signalScoringService.scoreStock(snapshot);
                    return WatchlistCoverageResponse.from(watchlist, latestSnapshotDate, result);
                })
                .toList();
    }

    /**
     * 관심 종목을 삭제한다.
     */
    @Transactional
    public void delete(CurrentUser currentUser, Long watchlistId) {
        Watchlist watchlist = watchlistRepository.findByIdAndUserId(watchlistId, currentUser.id())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "관심 종목을 찾을 수 없습니다."));
        watchlistRepository.delete(watchlist);
    }
}
