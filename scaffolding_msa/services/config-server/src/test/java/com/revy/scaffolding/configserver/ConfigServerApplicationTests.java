package com.revy.scaffolding.configserver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest(properties = {
    "eureka.client.enabled=false",
    "spring.cloud.discovery.enabled=false"
})
class ConfigServerApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void servesConfigUi() throws Exception {
        String response = mockMvc.perform(get("/config-ui"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThat(response).contains("Config Server UI");
    }

    @Test
    void servesNativeConfigRepository() throws Exception {
        String response = mockMvc.perform(get("/config/application/default"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThat(response).contains("\"name\":\"application\"");
    }
}

