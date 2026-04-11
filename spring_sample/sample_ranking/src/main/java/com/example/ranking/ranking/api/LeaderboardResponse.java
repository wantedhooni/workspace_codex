package com.example.ranking.ranking.api;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 시즌 리더보드 응답이다.
 */
public record LeaderboardResponse(
        String seasonId,
        int totalPlayers,
        LocalDateTime generatedAt,
        List<RankingEntryResponse> entries
) {
}
