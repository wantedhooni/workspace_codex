package com.example.ranking.ranking.api;

import com.example.ranking.ranking.application.RankingQueryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 랭킹 조회 API를 제공한다.
 */
@Validated
@RestController
@RequestMapping("/api/seasons/{seasonId}")
public class RankingQueryController {

    private final RankingQueryService rankingQueryService;

    public RankingQueryController(RankingQueryService rankingQueryService) {
        this.rankingQueryService = rankingQueryService;
    }

    @GetMapping("/leaderboard")
    public LeaderboardResponse getLeaderboard(
            @PathVariable String seasonId,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit
    ) {
        return rankingQueryService.getLeaderboard(seasonId, limit);
    }

    @GetMapping("/overview")
    public SeasonOverviewResponse getSeasonOverview(@PathVariable String seasonId) {
        return rankingQueryService.getSeasonOverview(seasonId);
    }

    @GetMapping("/players/{playerId}")
    public PlayerRankingDetailResponse getPlayerDetail(
            @PathVariable String seasonId,
            @PathVariable String playerId
    ) {
        return rankingQueryService.getPlayerDetail(seasonId, playerId);
    }

    @GetMapping("/players/{playerId}/neighbors")
    public AroundPlayerResponse getAroundPlayer(
            @PathVariable String seasonId,
            @PathVariable String playerId,
            @RequestParam(defaultValue = "2") @Min(1) @Max(10) int radius
    ) {
        return rankingQueryService.getAroundPlayer(seasonId, playerId, radius);
    }
}
