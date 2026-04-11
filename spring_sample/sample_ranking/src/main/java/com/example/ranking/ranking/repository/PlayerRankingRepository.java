package com.example.ranking.ranking.repository;

import com.example.ranking.ranking.domain.PlayerRanking;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 시즌별 랭킹 스냅샷 저장소다.
 */
public interface PlayerRankingRepository extends JpaRepository<PlayerRanking, Long> {

    Optional<PlayerRanking> findBySeasonIdAndPlayerId(String seasonId, String playerId);

    List<PlayerRanking> findBySeasonIdOrderByTotalScoreDescWinCountDescBestSingleScoreDescLastPlayedAtAscPlayerIdAsc(
            String seasonId
    );

    boolean existsBySeasonId(String seasonId);
}
