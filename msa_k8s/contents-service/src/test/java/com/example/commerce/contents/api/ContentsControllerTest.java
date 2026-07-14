package com.example.commerce.contents.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 콘텐츠 API의 작성, 수정, 발행, 버전 충돌, 커서 조회 계약을 통합 검증한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ContentsControllerTest {

    @Container
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:17-alpine"))
                    .withDatabaseName("commerce")
                    .withUsername("commerce")
                    .withPassword("commerce-test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 테스트 PostgreSQL 연결 정보를 Spring 환경에 등록한다.
     *
     * @param registry 동적 속성 레지스트리
     */
    @DynamicPropertySource
    static void infrastructureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    /**
     * 초안을 수정하고 발행한 뒤 과거 버전 요청이 충돌하는지 검증한다.
     *
     * @throws Exception MockMvc 요청 실패
     */
    @Test
    void updatesAndPublishesContentWithVersionCheck() throws Exception {
        JsonNode created = createContent("NOTICE", "금융 공지", "공지 초안");
        String id = created.get("id").asText();
        long initialVersion = created.get("version").asLong();

        MvcResult updatedResult = mockMvc.perform(put("/api/v1/contents/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"금융 공지 수정","body":"수정된 공지","version":%d}
                                """.formatted(initialVersion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("금융 공지 수정"))
                .andReturn();
        JsonNode updated = objectMapper.readTree(updatedResult.getResponse().getContentAsByteArray());

        mockMvc.perform(post("/api/v1/contents/{id}/publish", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"version":%d}
                                """.formatted(updated.get("version").asLong())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.publishedAt").exists());

        mockMvc.perform(put("/api/v1/contents/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"충돌","body":"오래된 수정","version":%d}
                                """.formatted(initialVersion)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONTENT_CONFLICT"));
    }

    /**
     * 콘텐츠 목록이 복합 커서를 반환하고 다음 페이지를 중복 없이 조회하는지 검증한다.
     *
     * @throws Exception MockMvc 요청 실패
     */
    @Test
    void listsContentsWithCursorPagination() throws Exception {
        createContent("POST", "게시물 1", "첫 번째 게시물");
        createContent("POST", "게시물 2", "두 번째 게시물");

        MvcResult firstPage = mockMvc.perform(get("/api/v1/contents")
                        .param("type", "POST")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.nextCursor").isNotEmpty())
                .andReturn();
        JsonNode first = objectMapper.readTree(firstPage.getResponse().getContentAsByteArray());
        String firstId = first.get("items").get(0).get("id").asText();

        mockMvc.perform(get("/api/v1/contents")
                        .param("type", "POST")
                        .param("size", "1")
                        .param("cursor", first.get("nextCursor").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].id").value(org.hamcrest.Matchers.not(firstId)));
    }

    private JsonNode createContent(String type, String title, String body) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/contents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type":"%s",
                                  "title":"%s",
                                  "body":"%s",
                                  "authorId":"%s"
                                }
                                """.formatted(type, title, body, UUID.randomUUID())))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsByteArray());
    }
}
