package com.example.ranking.config;

import com.example.ranking.ranking.api.ScoreSubmissionRequest;
import com.example.ranking.ranking.application.RankingCommandService;
import com.example.ranking.ranking.domain.MatchResult;
import com.example.ranking.ranking.repository.PlayerRankingRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 데모 확인을 위한 시즌/플레이어 시드 데이터를 적재한다.
 */
@Configuration
public class DataInitializer {

    @Bean
    @ConditionalOnProperty(name = "sample.ranking.seed.enabled", havingValue = "true", matchIfMissing = true)
    public ApplicationRunner rankingSeedRunner(
            RankingCommandService rankingCommandService,
            PlayerRankingRepository playerRankingRepository
    ) {
        return args -> {
            String seasonId = "season-2026-spring";
            if (playerRankingRepository.existsBySeasonId(seasonId)) {
                return;
            }

            LocalDateTime baseTime = LocalDateTime.of(2026, 4, 1, 10, 0);
            List<ScoreSubmissionRequest> requests = List.of(
                    new ScoreSubmissionRequest("player-100", "Astra", 1200, MatchResult.WIN, "오프닝 토너먼트 우승", baseTime.plusMinutes(5)),
                    new ScoreSubmissionRequest("player-101", "Blaze", 950, MatchResult.WIN, "랭킹전 3연승", baseTime.plusMinutes(12)),
                    new ScoreSubmissionRequest("player-102", "Cipher", 870, MatchResult.DRAW, "팀전 무승부", baseTime.plusMinutes(20)),
                    new ScoreSubmissionRequest("player-103", "Drift", 760, MatchResult.LOSS, "보스전 패배", baseTime.plusMinutes(32)),
                    new ScoreSubmissionRequest("player-104", "Echo", 1110, MatchResult.WIN, "주간 컵 2위", baseTime.plusMinutes(44)),
                    new ScoreSubmissionRequest("player-105", "Flux", 680, MatchResult.DRAW, "친선전 무승부", baseTime.plusMinutes(51)),
                    new ScoreSubmissionRequest("player-106", "Glint", 1040, MatchResult.WIN, "랭킹전 승리", baseTime.plusMinutes(63)),
                    new ScoreSubmissionRequest("player-107", "Halo", 900, MatchResult.WIN, "주말 챌린지 승리", baseTime.plusMinutes(78)),
                    new ScoreSubmissionRequest("player-100", "Astra", 320, MatchResult.WIN, "플레이오프 승리", baseTime.plusMinutes(95)),
                    new ScoreSubmissionRequest("player-101", "Blaze", 180, MatchResult.LOSS, "타이브레이커 패배", baseTime.plusMinutes(102)),
                    new ScoreSubmissionRequest("player-104", "Echo", 240, MatchResult.WIN, "보너스 스테이지", baseTime.plusMinutes(116)),
                    new ScoreSubmissionRequest("player-106", "Glint", 210, MatchResult.DRAW, "수비전 무승부", baseTime.plusMinutes(134))
            );

            for (ScoreSubmissionRequest request : requests) {
                rankingCommandService.submitScore(seasonId, request);
            }
        };
    }
}
