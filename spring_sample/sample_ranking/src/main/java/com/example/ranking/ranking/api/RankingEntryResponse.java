package com.example.ranking.ranking.api;

import java.time.LocalDateTime;

/**
 * 리더보드 단일 항목 응답이다.
 */
public record RankingEntryResponse(
        int rank,
        String playerId,
        String playerName,
        long totalScore,
        int bestSingleScore,
        int matchCount,
        int winCount,
        int lossCount,
        int drawCount,
        LocalDateTime lastPlayedAt,
        boolean focused
) {
}
