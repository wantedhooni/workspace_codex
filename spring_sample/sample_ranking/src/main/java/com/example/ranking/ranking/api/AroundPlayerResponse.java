package com.example.ranking.ranking.api;

import java.util.List;

/**
 * 특정 플레이어 주변 순위 응답이다.
 */
public record AroundPlayerResponse(
        String seasonId,
        String playerId,
        int radius,
        List<RankingEntryResponse> entries
) {
}
