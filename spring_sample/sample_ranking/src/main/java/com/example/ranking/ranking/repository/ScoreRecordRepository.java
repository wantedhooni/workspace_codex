package com.example.ranking.ranking.repository;

import com.example.ranking.ranking.domain.ScoreRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 점수 이벤트 이력 저장소다.
 */
public interface ScoreRecordRepository extends JpaRepository<ScoreRecord, Long> {

    List<ScoreRecord> findTop20BySeasonIdAndPlayerIdOrderByPlayedAtDescCreatedAtDesc(String seasonId, String playerId);

    long countBySeasonId(String seasonId);
}
