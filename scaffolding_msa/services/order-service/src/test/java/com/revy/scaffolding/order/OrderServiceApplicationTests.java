package com.revy.scaffolding.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.revy.scaffolding.order.client.UserServiceClient;
import com.revy.scaffolding.order.domain.repository.PurchaseOrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(properties = {
    "eureka.client.enabled=false",
    "spring.cloud.discovery.enabled=false"
})
class OrderServiceApplicationTests {
    @Autowired
    private PurchaseOrderRepository orderRepository;

    @MockitoBean
    private UserServiceClient userServiceClient;

    @Test
    void loadsSampleOrders() {
        assertThat(orderRepository.count()).isGreaterThanOrEqualTo(1);
    }
}
