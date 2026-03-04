package com.example.apigatewaywebflux;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class GatewayRoutingTests {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void proxiesCustomerServiceRequest() {
        webTestClient.get()
                .uri("/api/customer-service/customers/CUST-100")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Gateway-Processed", "true")
                .expectHeader().valueEquals("X-Gateway-App", "apigateway-webflux")
                .expectBody()
                .jsonPath("$.customerCode").isEqualTo("CUST-100");
    }

    @Test
    void exposesRouteSummaries() {
        webTestClient.get()
                .uri("/admin/routes")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].routeId").isEqualTo("customer-service");
    }
}
