package com.example.multitenancy;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * JWT와 tenant filter가 함께 동작하는지 검증한다.
 */
@SpringBootTest
@AutoConfigureMockMvc
class MultiTenancyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void alphaTenantSeesOnlyAlphaProjects() throws Exception {
        String token = login("alpha", "alpha.admin", "demo1234");

        mockMvc.perform(get("/api/projects/dashboard")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tenant.tenantId").value("alpha"))
                .andExpect(jsonPath("$.projects.length()").value(2))
                .andExpect(jsonPath("$.projects[0].name").exists())
                .andExpect(jsonPath("$.projects[?(@.name=='병원 예약 대시보드')]").isEmpty());
    }

    @Test
    void tenantCannotUpdateOtherTenantProject() throws Exception {
        String alphaToken = login("alpha", "alpha.admin", "demo1234");
        String betaToken = login("beta", "beta.admin", "demo1234");

        MvcResult createdResult = mockMvc.perform(post("/api/projects")
                        .with(csrf().asHeader())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + betaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "EMR 연동 개선",
                                  "description": "베타 테넌트 전용 연동 프로젝트",
                                  "ownerName": "김하린",
                                  "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(createdResult.getResponse().getContentAsString());
        long projectId = jsonNode.get("id").asLong();

        mockMvc.perform(patch("/api/projects/{projectId}/status", projectId)
                        .with(csrf().asHeader())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + alphaToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "COMPLETED"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("현재 테넌트에서 접근 가능한 프로젝트가 없습니다."));
    }

    private String login(String tenantId, String username, String password) throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tenantId": "%s",
                                  "username": "%s",
                                  "password": "%s"
                                }
                                """.formatted(tenantId, username, password)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        return jsonNode.get("accessToken").asText();
    }
}
