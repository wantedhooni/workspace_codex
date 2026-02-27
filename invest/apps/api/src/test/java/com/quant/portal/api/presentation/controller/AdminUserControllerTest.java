package com.quant.portal.api.presentation.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldManageAdminUsersAndAuthenticateWithCreatedUser() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users").with(httpBasic("user", "user1234")))
                .andExpect(status().isForbidden());

        String username = "qa_user_manage";
        String password = "qaUser1234";

        MvcResult createResult = mockMvc.perform(post("/api/v1/admin/users")
                        .with(httpBasic("admin", "admin1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "password": "%s",
                                  "displayName": "QA User",
                                  "roleCodes": ["ROLE_USER"],
                                  "enabled": true
                                }
                                """.formatted(username, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value(username))
                .andExpect(jsonPath("$.data.roleCodes[0]").value("ROLE_USER"))
                .andReturn();

        JsonNode createNode = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long userId = createNode.path("data").path("id").asLong();

        mockMvc.perform(get("/api/v1/admin/users/{id}", userId)
                        .with(httpBasic("admin", "admin1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.displayName").value("QA User"));

        mockMvc.perform(get("/api/v1/admin/users")
                        .with(httpBasic("admin", "admin1234"))
                        .param("filter", "{\"keyword\":\"qa_user\",\"roleCode\":\"ROLE_USER\"}")
                        .param("page", "1")
                        .param("perPage", "25")
                        .param("sort", "createdAt,DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.data[0].id").value(userId));

        mockMvc.perform(put("/api/v1/admin/users/{id}", userId)
                        .with(httpBasic("admin", "admin1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "displayName": "QA User Updated",
                                  "roleCodes": ["ROLE_USER"],
                                  "enabled": true,
                                  "newPassword": "qaUser9999"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.displayName").value("QA User Updated"));

        mockMvc.perform(get("/api/v1/users/me").with(httpBasic(username, "qaUser9999")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value(username))
                .andExpect(jsonPath("$.data.roles[0]").value("ROLE_USER"));

        mockMvc.perform(delete("/api/v1/admin/users/{id}", userId)
                        .with(httpBasic("admin", "admin1234")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/admin/users/{id}", userId)
                        .with(httpBasic("admin", "admin1234")))
                .andExpect(status().isNotFound());
    }
}

