package com.example.transactionaloutbox.bootstrap;

import com.example.transactionaloutbox.order.api.CreateOrderRequest;
import com.example.transactionaloutbox.order.application.OrderCommandService;
import com.example.transactionaloutbox.order.domain.PurchaseOrderRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 로컬에서 Outbox 흐름을 바로 확인할 수 있도록 샘플 주문과 이벤트를 만든다.
 */
@Configuration
@Profile("!test")
public class SampleDataInitializer {

    /**
     * 샘플 주문을 초기 적재하는 러너를 만든다.
     *
     * @param purchaseOrderRepository 주문 존재 여부 확인용 저장소
     * @param orderCommandService 주문 생성 서비스
     * @return 애플리케이션 시작 후 실행되는 러너
     */
    @Bean
    CommandLineRunner transactionalOutboxDataLoader(
            PurchaseOrderRepository purchaseOrderRepository,
            OrderCommandService orderCommandService
    ) {
        return args -> {
            if (purchaseOrderRepository.count() > 0) {
                return;
            }

            orderCommandService.createOrder(new CreateOrderRequest("CUST-900", "SKU-OUTBOX-1", 2, new BigDecimal("129000")));
            orderCommandService.createOrder(new CreateOrderRequest("CUST-901", "SKU-OUTBOX-2", 1, new BigDecimal("249000")));
            orderCommandService.createOrder(new CreateOrderRequest("CUST-902", "SKU-OUTBOX-3", 4, new BigDecimal("45000")));
        };
    }
}
