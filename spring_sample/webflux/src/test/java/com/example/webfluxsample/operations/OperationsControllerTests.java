package com.example.webfluxsample.operations;

import static org.mockito.BDDMockito.given;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = OperationsController.class)
class OperationsControllerTests {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private OperationsService operationsService;

    @Test
    void returnsDashboard() {
        OperationsDashboardResponse response = new OperationsDashboardResponse(
                "payments",
                420,
                0.4d,
                95,
                "STABLE"
        );

        given(operationsService.getDashboard("payments")).willReturn(Mono.just(response));

        webTestClient.get()
                .uri("/api/operations/dashboard?domain=payments")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.domain").isEqualTo("payments")
                .jsonPath("$.status").isEqualTo("STABLE");
    }

    @Test
    void streamsEvents() {
        given(operationsService.streamEvents("payments")).willReturn(Flux.just(
                new OperationsEventResponse("payments", "INFO", "consumer lag 정상 범위 유지", OffsetDateTime.now())
        ));

        webTestClient.get()
                .uri("/api/operations/events/stream?domain=payments")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> org.assertj.core.api.Assertions.assertThat(body).contains("operations-event"));
    }
}
