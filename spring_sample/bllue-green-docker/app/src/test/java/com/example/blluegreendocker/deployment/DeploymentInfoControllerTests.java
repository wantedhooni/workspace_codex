package com.example.blluegreendocker.deployment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "DEPLOY_COLOR=blue",
        "DEPLOY_VERSION=test-docker"
})
class DeploymentInfoControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnDeploymentData() throws Exception {
        mockMvc.perform(get("/api/deployment"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application").value("bllue-green-docker-app"))
                .andExpect(jsonPath("$.color").value("blue"))
                .andExpect(jsonPath("$.version").value("test-docker"));
    }
}
