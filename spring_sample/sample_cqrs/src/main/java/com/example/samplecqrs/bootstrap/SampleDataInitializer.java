package com.example.samplecqrs.bootstrap;

import com.example.samplecqrs.command.api.CreateOrderRequest;
import com.example.samplecqrs.command.api.OrderCommandResponse;
import com.example.samplecqrs.command.application.OrderCommandService;
import com.example.samplecqrs.command.domain.PurchaseOrderRepository;
import java.math.BigDecimal;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * 로컬 실행 시 CQRS 예제를 바로 확인할 수 있도록 샘플 주문 데이터를 적재한다.
 */
@Configuration
@Profile("!test")
public class SampleDataInitializer {

    /**
     * 주문과 조회 프로젝션을 함께 생성하는 샘플 데이터 적재 러너를 만든다.
     *
     * @param purchaseOrderRepository 주문 존재 여부 확인용 저장소
     * @param orderCommandService 주문 생성/취소 서비스
     * @return 애플리케이션 시작 후 실행되는 러너
     */
    @Bean
    CommandLineRunner sampleCqrsDataLoader(
            PurchaseOrderRepository purchaseOrderRepository,
            OrderCommandService orderCommandService
    ) {
        return args -> {
            if (purchaseOrderRepository.count() > 0) {
                return;
            }

            orderCommandService.createOrder(new CreateOrderRequest("CUST-100", "MONITOR-32", 2, new BigDecimal("320000")));
            orderCommandService.createOrder(new CreateOrderRequest("CUST-101", "KEYBOARD-MECH", 5, new BigDecimal("119000")));
            OrderCommandResponse cancelled = orderCommandService.createOrder(
                    new CreateOrderRequest("CUST-102", "DOCK-USB-C", 1, new BigDecimal("159000"))
            );
            orderCommandService.cancelOrder(cancelled.orderId());
        };
    }
}
