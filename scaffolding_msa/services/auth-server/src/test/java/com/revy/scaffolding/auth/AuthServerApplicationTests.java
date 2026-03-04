package com.revy.scaffolding.auth;

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
class AuthServerApplicationTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void openidConfigurationRequiresNoAuthentication() throws Exception {
        String response = mockMvc.perform(get("/.well-known/openid-configuration"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();

        assertThat(response).contains("\"issuer\":\"http://localhost:9000\"");
    }
}
