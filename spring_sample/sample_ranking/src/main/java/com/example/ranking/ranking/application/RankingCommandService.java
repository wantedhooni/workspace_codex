package com.example.ranking.ranking.application;

import com.example.ranking.ranking.api.PlayerRankingDetailResponse;
import com.example.ranking.ranking.api.ScoreSubmissionRequest;
import com.example.ranking.ranking.domain.PlayerRanking;
import com.example.ranking.ranking.domain.ScoreRecord;
import com.example.ranking.ranking.repository.PlayerRankingRepository;
import com.example.ranking.ranking.repository.ScoreRecordRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 랭킹 점수 적재와 스냅샷 갱신을 담당하는 서비스다.
 */
@Service
@Transactional
public class RankingCommandService {

    private final PlayerRankingRepository playerRankingRepository;
    private final ScoreRecordRepository scoreRecordRepository;
    private final RankingQueryService rankingQueryService;

    public RankingCommandService(
            PlayerRankingRepository playerRankingRepository,
            ScoreRecordRepository scoreRecordRepository,
            RankingQueryService rankingQueryService
    ) {
        this.playerRankingRepository = playerRankingRepository;
        this.scoreRecordRepository = scoreRecordRepository;
        this.rankingQueryService = rankingQueryService;
    }

    /**
     * 플레이어의 경기 결과를 기록하고 시즌 랭킹을 갱신한다.
     */
    public PlayerRankingDetailResponse submitScore(String seasonId, ScoreSubmissionRequest request) {
        LocalDateTime playedAt = request.playedAt() == null ? LocalDateTime.now() : request.playedAt();

        PlayerRanking playerRanking = playerRankingRepository.findBySeasonIdAndPlayerId(seasonId, request.playerId())
                .orElseGet(() -> PlayerRanking.create(seasonId, request.playerId(), request.playerName(), playedAt));

        playerRanking.applyScore(request.scoreDelta(), request.result(), playedAt, request.playerName());

        playerRankingRepository.save(playerRanking);
        scoreRecordRepository.save(ScoreRecord.create(
                seasonId,
                request.playerId(),
                request.playerName(),
                request.scoreDelta(),
                request.result(),
                request.memo(),
                playedAt
        ));

        return rankingQueryService.getPlayerDetail(seasonId, request.playerId());
    }
}
