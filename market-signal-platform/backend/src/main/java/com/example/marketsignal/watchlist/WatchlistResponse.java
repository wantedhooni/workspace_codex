package com.example.marketsignal.watchlist;

import java.time.LocalDateTime;

/**
 * 관심 종목 응답 모델이다.
 */
public record WatchlistResponse(
        Long id,
        String ticker,
        String companyName,
        LocalDateTime createdAt
) {

    public static WatchlistResponse from(Watchlist watchlist) {
        return new WatchlistResponse(
                watchlist.getId(),
                watchlist.getTicker(),
                watchlist.getCompanyName(),
                watchlist.getCreatedAt()
        );
    }
}
