package com.example.ranking.ranking.api;

import com.example.ranking.ranking.application.RankingCommandService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 랭킹 점수 등록 API를 제공한다.
 */
@RestController
@RequestMapping("/api/seasons/{seasonId}")
public class RankingCommandController {

    private final RankingCommandService rankingCommandService;

    public RankingCommandController(RankingCommandService rankingCommandService) {
        this.rankingCommandService = rankingCommandService;
    }

    @PostMapping("/scores")
    @ResponseStatus(HttpStatus.CREATED)
    public PlayerRankingDetailResponse submitScore(
            @PathVariable String seasonId,
            @Valid @RequestBody ScoreSubmissionRequest request
    ) {
        return rankingCommandService.submitScore(seasonId, request);
    }
}
