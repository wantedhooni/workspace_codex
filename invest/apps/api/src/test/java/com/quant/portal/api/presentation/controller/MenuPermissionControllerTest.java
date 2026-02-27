package com.quant.portal.api.presentation.controller;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
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
class MenuPermissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRestrictUserAndAllowAdminCrudForMenuPermissions() throws Exception {
        mockMvc.perform(get("/api/v1/admin/menu-permissions").with(httpBasic("user", "user1234")))
                .andExpect(status().isForbidden());

        long portfoliosPermissionId = createMenuPermission("ROLE_USER", "portfolios", true, false, true, false);
        long transactionsPermissionId = createMenuPermission("ROLE_USER", "transactions", true, false, false, false);

        mockMvc.perform(get("/api/v1/admin/menu-permissions/{id}", portfoliosPermissionId)
                        .with(httpBasic("admin", "admin1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.roleCode").value("ROLE_USER"))
                .andExpect(jsonPath("$.data.menuKey").value("portfolios"))
                .andExpect(jsonPath("$.data.canEdit").value(true));

        mockMvc.perform(get("/api/v1/admin/menu-permissions")
                        .with(httpBasic("admin", "admin1234"))
                        .param("filter", "{\"roleCode\":\"ROLE_USER\"}")
                        .param("sort", "createdAt,DESC")
                        .param("page", "1")
                        .param("perPage", "25"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(2));

        mockMvc.perform(put("/api/v1/admin/menu-permissions/{id}", transactionsPermissionId)
                        .with(httpBasic("admin", "admin1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roleCode": "ROLE_USER",
                                  "menuKey": "transactions",
                                  "canList": true,
                                  "canCreate": true,
                                  "canEdit": false,
                                  "canDelete": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.canCreate").value(true));

        mockMvc.perform(put("/api/v1/admin/menu-permissions/{id}", transactionsPermissionId)
                        .with(httpBasic("admin", "admin1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roleCode": "ROLE_USER",
                                  "menuKey": "portfolios",
                                  "canList": true,
                                  "canCreate": false,
                                  "canEdit": false,
                                  "canDelete": false
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("CONFLICT"));

        mockMvc.perform(delete("/api/v1/admin/menu-permissions/{id}", transactionsPermissionId)
                        .with(httpBasic("admin", "admin1234")))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldResolveCurrentUserMenuPermissions() throws Exception {
        createMenuPermission("ROLE_USER", "transactions", true, false, false, false);
        createMenuPermission("ROLE_USER", "macro-indicators", true, true, false, false);

        MvcResult userPermissionResult = mockMvc.perform(get("/api/v1/users/me/menu-permissions")
                        .with(httpBasic("user", "user1234")))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, JsonNode> userPermissions = toPermissionMap(userPermissionResult);
        assertTrue(userPermissions.containsKey("transactions"));
        assertFalse(userPermissions.get("transactions").path("canCreate").asBoolean());
        assertTrue(userPermissions.containsKey("macro-indicators"));
        assertTrue(userPermissions.get("macro-indicators").path("canCreate").asBoolean());

        MvcResult adminPermissionResult = mockMvc.perform(get("/api/v1/users/me/menu-permissions")
                        .with(httpBasic("admin", "admin1234")))
                .andExpect(status().isOk())
                .andReturn();

        Map<String, JsonNode> adminPermissions = toPermissionMap(adminPermissionResult);
        assertTrue(adminPermissions.containsKey("dashboard"));
        assertTrue(adminPermissions.get("dashboard").path("canList").asBoolean());
        assertTrue(adminPermissions.containsKey("menu-permissions"));
        assertTrue(adminPermissions.get("menu-permissions").path("canEdit").asBoolean());
    }

    private long createMenuPermission(
            String roleCode,
            String menuKey,
            boolean canList,
            boolean canCreate,
            boolean canEdit,
            boolean canDelete
    ) throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/v1/admin/menu-permissions")
                        .with(httpBasic("admin", "admin1234"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "roleCode": "%s",
                                  "menuKey": "%s",
                                  "canList": %s,
                                  "canCreate": %s,
                                  "canEdit": %s,
                                  "canDelete": %s
                                }
                                """.formatted(roleCode, menuKey, canList, canCreate, canEdit, canDelete)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode responseNode = objectMapper.readTree(createResult.getResponse().getContentAsString());
        return responseNode.path("data").path("id").asLong();
    }

    private Map<String, JsonNode> toPermissionMap(MvcResult result) throws Exception {
        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        Map<String, JsonNode> map = new HashMap<>();
        for (JsonNode permission : root.path("data")) {
            map.put(permission.path("menuKey").asText(), permission);
        }
        return map;
    }
}

