package com.example.r2dbcsample.customer;

import static org.mockito.BDDMockito.given;

import com.example.r2dbcsample.support.ApiExceptionHandler;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Import(ApiExceptionHandler.class)
@WebFluxTest(controllers = CustomerController.class)
class CustomerControllerTests {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CustomerCommandService customerCommandService;

    @Test
    void returnsCustomerList() {
        CustomerResponse response = new CustomerResponse(
                "CUST-100",
                "Kim Minji",
                "minji.kim@example.com",
                CustomerTier.STANDARD,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(customerCommandService.getCustomers()).willReturn(Flux.just(response));

        webTestClient.get()
                .uri("/api/customers")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].customerCode").isEqualTo("CUST-100");
    }

    @Test
    void createsCustomer() {
        CustomerResponse response = new CustomerResponse(
                "CUST-300",
                "Han Sujin",
                "sujin.han@example.com",
                CustomerTier.VIP,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(customerCommandService.createCustomer(new CreateCustomerRequest(
                "CUST-300",
                "Han Sujin",
                "sujin.han@example.com",
                CustomerTier.VIP
        ))).willReturn(Mono.just(response));

        webTestClient.post()
                .uri("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "customerCode": "CUST-300",
                          "name": "Han Sujin",
                          "email": "sujin.han@example.com",
                          "tier": "VIP"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.customerCode").isEqualTo("CUST-300")
                .jsonPath("$.tier").isEqualTo("VIP");
    }
}
