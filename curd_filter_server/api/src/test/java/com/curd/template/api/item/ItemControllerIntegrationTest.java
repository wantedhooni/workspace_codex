package com.curd.template.api.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers(disabledWithoutDocker = true)
class ItemControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @LocalServerPort
    int port;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Test
    void searchWithFilters() throws Exception {
        createItem("{\"name\":\"alpha-phone\",\"description\":\"alpha\",\"price\":100.00}");
        createItem("{\"name\":\"beta-phone\",\"description\":\"beta\",\"price\":200.00}");

        HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create("http://localhost:" + port + "/api/v1/items?and=name:contains:phone&and=price:gte:150&sort=-price"))
            .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"name\":\"beta-phone\""));
        assertFalse(response.body().contains("\"name\":\"alpha-phone\""));
    }

    private void createItem(String jsonBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
            .uri(URI.create("http://localhost:" + port + "/api/v1/items"))
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode(), response.body());
    }
}
