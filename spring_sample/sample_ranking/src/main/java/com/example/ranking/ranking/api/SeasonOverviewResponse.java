package com.example.ranking.ranking.api;

import java.time.LocalDateTime;

/**
 * 시즌 운영 개요 응답이다.
 */
public record SeasonOverviewResponse(
        String seasonId,
        int totalPlayers,
        long totalMatches,
        long totalScore,
        String topPlayerId,
        String topPlayerName,
        long topScore,
        LocalDateTime updatedAt
) {
}
