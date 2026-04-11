package com.example.ranking.ranking.application;

import com.example.ranking.common.RankingNotFoundException;
import com.example.ranking.ranking.api.AroundPlayerResponse;
import com.example.ranking.ranking.api.LeaderboardResponse;
import com.example.ranking.ranking.api.PlayerRankingDetailResponse;
import com.example.ranking.ranking.api.RankingEntryResponse;
import com.example.ranking.ranking.api.SeasonOverviewResponse;
import com.example.ranking.ranking.domain.PlayerRanking;
import com.example.ranking.ranking.domain.ScoreRecord;
import com.example.ranking.ranking.repository.PlayerRankingRepository;
import com.example.ranking.ranking.repository.ScoreRecordRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 랭킹 조회 시나리오를 제공하는 서비스다.
 */
@Service
@Transactional(readOnly = true)
public class RankingQueryService {

    private final PlayerRankingRepository playerRankingRepository;
    private final ScoreRecordRepository scoreRecordRepository;

    public RankingQueryService(
            PlayerRankingRepository playerRankingRepository,
            ScoreRecordRepository scoreRecordRepository
    ) {
        this.playerRankingRepository = playerRankingRepository;
        this.scoreRecordRepository = scoreRecordRepository;
    }

    /**
     * 시즌 상위 리더보드를 조회한다.
     */
    public LeaderboardResponse getLeaderboard(String seasonId, int limit) {
        List<RankedPlayer> rankedPlayers = buildRankedPlayers(seasonId);
        List<RankingEntryResponse> entries = rankedPlayers.stream()
                .limit(limit)
                .map(rankedPlayer -> toEntryResponse(rankedPlayer, false))
                .toList();

        return new LeaderboardResponse(seasonId, rankedPlayers.size(), LocalDateTime.now(), entries);
    }

    /**
     * 특정 플레이어의 상세 랭킹과 최근 이력을 조회한다.
     */
    public PlayerRankingDetailResponse getPlayerDetail(String seasonId, String playerId) {
        List<RankedPlayer> rankedPlayers = buildRankedPlayers(seasonId);
        RankedPlayer rankedPlayer = rankedPlayers.stream()
                .filter(player -> player.player().getPlayerId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new RankingNotFoundException("해당 플레이어의 랭킹을 찾을 수 없습니다."));

        List<PlayerRankingDetailResponse.RecentMatchResponse> recentMatches = scoreRecordRepository
                .findTop20BySeasonIdAndPlayerIdOrderByPlayedAtDescCreatedAtDesc(seasonId, playerId)
                .stream()
                .map(this::toRecentMatchResponse)
                .toList();

        PlayerRanking player = rankedPlayer.player();
        return new PlayerRankingDetailResponse(
                seasonId,
                player.getPlayerId(),
                player.getPlayerName(),
                rankedPlayer.rank(),
                rankedPlayers.size(),
                player.getTotalScore(),
                player.getBestSingleScore(),
                player.getMatchCount(),
                player.getWinCount(),
                player.getLossCount(),
                player.getDrawCount(),
                calculateTopPercent(rankedPlayer.rank(), rankedPlayers.size()),
                player.getLastPlayedAt(),
                recentMatches
        );
    }

    /**
     * 특정 플레이어를 중심으로 주변 순위를 조회한다.
     */
    public AroundPlayerResponse getAroundPlayer(String seasonId, String playerId, int radius) {
        List<RankedPlayer> rankedPlayers = buildRankedPlayers(seasonId);
        int index = findPlayerIndex(rankedPlayers, playerId)
                .orElseThrow(() -> new RankingNotFoundException("해당 플레이어의 랭킹을 찾을 수 없습니다."));

        int fromIndex = Math.max(0, index - radius);
        int toIndex = Math.min(rankedPlayers.size(), index + radius + 1);
        List<RankingEntryResponse> entries = rankedPlayers.subList(fromIndex, toIndex)
                .stream()
                .map(rankedPlayer -> toEntryResponse(rankedPlayer, rankedPlayer.player().getPlayerId().equals(playerId)))
                .toList();

        return new AroundPlayerResponse(seasonId, playerId, radius, entries);
    }

    /**
     * 시즌 운영 지표를 집계한다.
     */
    public SeasonOverviewResponse getSeasonOverview(String seasonId) {
        List<RankedPlayer> rankedPlayers = buildRankedPlayers(seasonId);
        RankedPlayer topPlayer = rankedPlayers.get(0);

        long totalMatches = scoreRecordRepository.countBySeasonId(seasonId);
        long totalScore = rankedPlayers.stream()
                .map(RankedPlayer::player)
                .mapToLong(PlayerRanking::getTotalScore)
                .sum();

        return new SeasonOverviewResponse(
                seasonId,
                rankedPlayers.size(),
                totalMatches,
                totalScore,
                topPlayer.player().getPlayerId(),
                topPlayer.player().getPlayerName(),
                topPlayer.player().getTotalScore(),
                topPlayer.player().getUpdatedAt()
        );
    }

    private List<RankedPlayer> buildRankedPlayers(String seasonId) {
        List<PlayerRanking> players = playerRankingRepository
                .findBySeasonIdOrderByTotalScoreDescWinCountDescBestSingleScoreDescLastPlayedAtAscPlayerIdAsc(seasonId);

        if (players.isEmpty()) {
            throw new RankingNotFoundException("해당 시즌의 랭킹 데이터가 없습니다.");
        }

        List<RankedPlayer> rankedPlayers = new ArrayList<>();
        int currentRank = 0;
        PlayerRanking previous = null;
        for (int index = 0; index < players.size(); index++) {
            PlayerRanking current = players.get(index);
            if (previous == null || hasDifferentRank(previous, current)) {
                currentRank = index + 1;
            }
            rankedPlayers.add(new RankedPlayer(currentRank, current));
            previous = current;
        }
        return rankedPlayers;
    }

    private boolean hasDifferentRank(PlayerRanking left, PlayerRanking right) {
        return left.getTotalScore() != right.getTotalScore()
                || left.getWinCount() != right.getWinCount()
                || left.getBestSingleScore() != right.getBestSingleScore()
                || !left.getLastPlayedAt().isEqual(right.getLastPlayedAt());
    }

    private Optional<Integer> findPlayerIndex(List<RankedPlayer> rankedPlayers, String playerId) {
        for (int index = 0; index < rankedPlayers.size(); index++) {
            if (rankedPlayers.get(index).player().getPlayerId().equals(playerId)) {
                return Optional.of(index);
            }
        }
        return Optional.empty();
    }

    private RankingEntryResponse toEntryResponse(RankedPlayer rankedPlayer, boolean focused) {
        PlayerRanking player = rankedPlayer.player();
        return new RankingEntryResponse(
                rankedPlayer.rank(),
                player.getPlayerId(),
                player.getPlayerName(),
                player.getTotalScore(),
                player.getBestSingleScore(),
                player.getMatchCount(),
                player.getWinCount(),
                player.getLossCount(),
                player.getDrawCount(),
                player.getLastPlayedAt(),
                focused
        );
    }

    private PlayerRankingDetailResponse.RecentMatchResponse toRecentMatchResponse(ScoreRecord scoreRecord) {
        return new PlayerRankingDetailResponse.RecentMatchResponse(
                scoreRecord.getScoreDelta(),
                scoreRecord.getResult(),
                scoreRecord.getMemo(),
                scoreRecord.getPlayedAt()
        );
    }

    private double calculateTopPercent(int rank, int totalPlayers) {
        if (totalPlayers <= 1) {
            return 100.0;
        }

        double percentile = ((double) (totalPlayers - rank) / (double) (totalPlayers - 1)) * 100.0;
        return Math.round(percentile * 10.0) / 10.0;
    }

    private record RankedPlayer(int rank, PlayerRanking player) {
    }
}
