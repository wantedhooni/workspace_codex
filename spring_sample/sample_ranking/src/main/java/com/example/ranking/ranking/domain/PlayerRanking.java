package com.example.ranking.ranking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 시즌별 플레이어 랭킹 스냅샷을 저장한다.
 */
@Entity
@Table(
        name = "player_ranking",
        indexes = {
                @Index(name = "idx_player_ranking_season", columnList = "seasonId"),
                @Index(name = "idx_player_ranking_season_player", columnList = "seasonId,playerId", unique = true)
        }
)
public class PlayerRanking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String seasonId;

    @Column(nullable = false, length = 50)
    private String playerId;

    @Column(nullable = false, length = 100)
    private String playerName;

    @Column(nullable = false)
    private long totalScore;

    @Column(nullable = false)
    private int bestSingleScore;

    @Column(nullable = false)
    private int matchCount;

    @Column(nullable = false)
    private int winCount;

    @Column(nullable = false)
    private int lossCount;

    @Column(nullable = false)
    private int drawCount;

    @Column(nullable = false)
    private LocalDateTime lastPlayedAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    protected PlayerRanking() {
    }

    private PlayerRanking(String seasonId, String playerId, String playerName, LocalDateTime playedAt) {
        this.seasonId = seasonId;
        this.playerId = playerId;
        this.playerName = playerName;
        this.totalScore = 0L;
        this.bestSingleScore = 0;
        this.matchCount = 0;
        this.winCount = 0;
        this.lossCount = 0;
        this.drawCount = 0;
        this.lastPlayedAt = playedAt;
        this.updatedAt = playedAt;
    }

    public static PlayerRanking create(String seasonId, String playerId, String playerName, LocalDateTime playedAt) {
        return new PlayerRanking(seasonId, playerId, playerName, playedAt);
    }

    /**
     * 경기 결과를 누적 반영해 랭킹 스냅샷을 갱신한다.
     */
    public void applyScore(int scoreDelta, MatchResult result, LocalDateTime playedAt, String playerName) {
        this.playerName = playerName;
        this.totalScore += scoreDelta;
        this.bestSingleScore = Math.max(this.bestSingleScore, scoreDelta);
        this.matchCount += 1;
        this.lastPlayedAt = playedAt;
        this.updatedAt = LocalDateTime.now();

        switch (result) {
            case WIN -> this.winCount += 1;
            case LOSS -> this.lossCount += 1;
            case DRAW -> this.drawCount += 1;
        }
    }

    public Long getId() {
        return id;
    }

    public String getSeasonId() {
        return seasonId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public long getTotalScore() {
        return totalScore;
    }

    public int getBestSingleScore() {
        return bestSingleScore;
    }

    public int getMatchCount() {
        return matchCount;
    }

    public int getWinCount() {
        return winCount;
    }

    public int getLossCount() {
        return lossCount;
    }

    public int getDrawCount() {
        return drawCount;
    }

    public LocalDateTime getLastPlayedAt() {
        return lastPlayedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
