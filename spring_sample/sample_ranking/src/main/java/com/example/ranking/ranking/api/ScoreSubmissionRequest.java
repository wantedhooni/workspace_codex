package com.example.ranking.ranking.api;

import com.example.ranking.ranking.domain.MatchResult;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 점수 등록 요청이다.
 */
public record ScoreSubmissionRequest(
        @NotBlank(message = "playerId는 필수입니다.")
        String playerId,
        @NotBlank(message = "playerName은 필수입니다.")
        String playerName,
        @Min(value = 0, message = "scoreDelta는 0 이상이어야 합니다.")
        @Max(value = 10000, message = "scoreDelta는 10000 이하여야 합니다.")
        int scoreDelta,
        @NotNull(message = "result는 필수입니다.")
        MatchResult result,
        String memo,
        LocalDateTime playedAt
) {
}
