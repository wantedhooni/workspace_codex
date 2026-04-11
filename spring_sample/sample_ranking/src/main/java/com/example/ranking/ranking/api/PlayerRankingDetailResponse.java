package com.example.ranking.ranking.api;

import com.example.ranking.ranking.domain.MatchResult;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 특정 플레이어의 랭킹 상세 응답이다.
 */
public record PlayerRankingDetailResponse(
        String seasonId,
        String playerId,
        String playerName,
        int rank,
        int totalPlayers,
        long totalScore,
        int bestSingleScore,
        int matchCount,
        int winCount,
        int lossCount,
        int drawCount,
        double topPercent,
        LocalDateTime lastPlayedAt,
        List<RecentMatchResponse> recentMatches
) {

    /**
     * 최근 경기 이력 응답이다.
     */
    public record RecentMatchResponse(
            int scoreDelta,
            MatchResult result,
            String memo,
            LocalDateTime playedAt
    ) {
    }
}
