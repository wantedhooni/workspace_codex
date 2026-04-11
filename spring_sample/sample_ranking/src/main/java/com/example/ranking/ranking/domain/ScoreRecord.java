package com.example.ranking.ranking.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 랭킹 변동 이력을 추적하는 점수 이벤트 엔티티다.
 */
@Entity
@Table(
        name = "score_record",
        indexes = {
                @Index(name = "idx_score_record_season_player", columnList = "seasonId,playerId"),
                @Index(name = "idx_score_record_played_at", columnList = "playedAt")
        }
)
public class ScoreRecord {

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
    private int scoreDelta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchResult result;

    @Column(length = 200)
    private String memo;

    @Column(nullable = false)
    private LocalDateTime playedAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected ScoreRecord() {
    }

    private ScoreRecord(
            String seasonId,
            String playerId,
            String playerName,
            int scoreDelta,
            MatchResult result,
            String memo,
            LocalDateTime playedAt
    ) {
        this.seasonId = seasonId;
        this.playerId = playerId;
        this.playerName = playerName;
        this.scoreDelta = scoreDelta;
        this.result = result;
        this.memo = memo;
        this.playedAt = playedAt;
        this.createdAt = LocalDateTime.now();
    }

    public static ScoreRecord create(
            String seasonId,
            String playerId,
            String playerName,
            int scoreDelta,
            MatchResult result,
            String memo,
            LocalDateTime playedAt
    ) {
        return new ScoreRecord(seasonId, playerId, playerName, scoreDelta, result, memo, playedAt);
    }

    public int getScoreDelta() {
        return scoreDelta;
    }

    public MatchResult getResult() {
        return result;
    }

    public String getMemo() {
        return memo;
    }

    public LocalDateTime getPlayedAt() {
        return playedAt;
    }
}
