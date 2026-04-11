package com.example.ranking.ranking;

import com.example.ranking.ranking.repository.PlayerRankingRepository;
import com.example.ranking.ranking.repository.ScoreRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 랭킹 API의 핵심 흐름을 검증하는 통합 테스트다.
 */
@SpringBootTest(properties = {
        "sample.ranking.seed.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:ranking-test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@AutoConfigureMockMvc
class RankingApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlayerRankingRepository playerRankingRepository;

    @Autowired
    private ScoreRecordRepository scoreRecordRepository;

    @BeforeEach
    void setUp() {
        scoreRecordRepository.deleteAll();
        playerRankingRepository.deleteAll();
    }

    @Test
    void 점수_등록_후_리더보드_순위가_정렬된다() throws Exception {
        submit("season-test", "player-a", "Alpha", 400, "WIN");
        submit("season-test", "player-b", "Bravo", 880, "WIN");
        submit("season-test", "player-c", "Charlie", 620, "DRAW");

        mockMvc.perform(get("/api/seasons/season-test/leaderboard")
                        .param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPlayers").value(3))
                .andExpect(jsonPath("$.entries[0].playerId").value("player-b"))
                .andExpect(jsonPath("$.entries[0].rank").value(1))
                .andExpect(jsonPath("$.entries[1].playerId").value("player-c"))
                .andExpect(jsonPath("$.entries[1].rank").value(2))
                .andExpect(jsonPath("$.entries[2].playerId").value("player-a"))
                .andExpect(jsonPath("$.entries[2].rank").value(3));
    }

    @Test
    void 내_주변_순위를_조회할_수_있다() throws Exception {
        submit("season-test", "player-a", "Alpha", 950, "WIN");
        submit("season-test", "player-b", "Bravo", 900, "WIN");
        submit("season-test", "player-c", "Charlie", 850, "DRAW");
        submit("season-test", "player-d", "Delta", 800, "LOSS");
        submit("season-test", "player-e", "Echo", 750, "WIN");

        mockMvc.perform(get("/api/seasons/season-test/players/player-c/neighbors")
                        .param("radius", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entries.length()").value(3))
                .andExpect(jsonPath("$.entries[0].playerId").value("player-b"))
                .andExpect(jsonPath("$.entries[1].playerId").value("player-c"))
                .andExpect(jsonPath("$.entries[1].focused").value(true))
                .andExpect(jsonPath("$.entries[2].playerId").value("player-d"));
    }

    private void submit(String seasonId, String playerId, String playerName, int scoreDelta, String result) throws Exception {
        mockMvc.perform(post("/api/seasons/{seasonId}/scores", seasonId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "playerId": "%s",
                                  "playerName": "%s",
                                  "scoreDelta": %d,
                                  "result": "%s",
                                  "memo": "테스트 데이터"
                                }
                                """.formatted(playerId, playerName, scoreDelta, result)))
                .andExpect(status().isCreated());
    }
}
