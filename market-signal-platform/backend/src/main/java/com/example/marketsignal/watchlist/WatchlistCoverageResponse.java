package com.example.marketsignal.watchlist;

import com.example.marketsignal.signal.SignalAction;
import com.example.marketsignal.signal.SignalResult;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 관심 종목과 최신 시그널 커버리지를 함께 반환하는 응답 모델이다.
 */
public record WatchlistCoverageResponse(
        Long id,
        String ticker,
        String companyName,
        LocalDateTime createdAt,
        boolean signalAvailable,
        LocalDate latestSignalDate,
        Integer score,
        SignalAction action,
        List<String> reasons
) {

    public static WatchlistCoverageResponse from(Watchlist watchlist, LocalDate latestSignalDate, SignalResult result) {
        return new WatchlistCoverageResponse(
                watchlist.getId(),
                watchlist.getTicker(),
                watchlist.getCompanyName(),
                watchlist.getCreatedAt(),
                result != null,
                result == null ? null : latestSignalDate,
                result == null ? null : result.score(),
                result == null ? null : result.action(),
                result == null
                        ? List.of()
                        : Arrays.stream(result.reasons().split(","))
                                .map(String::trim)
                                .filter(reason -> !reason.isBlank())
                                .toList()
        );
    }
}
