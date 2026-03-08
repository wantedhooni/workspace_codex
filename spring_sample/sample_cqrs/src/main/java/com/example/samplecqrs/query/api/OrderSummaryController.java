package com.example.samplecqrs.query.api;

import com.example.samplecqrs.query.application.OrderSummaryQueryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 조회 모델 전용 주문 API를 제공한다.
 */
@RestController
@RequestMapping("/api/order-summaries")
public class OrderSummaryController {

    private final OrderSummaryQueryService orderSummaryQueryService;

    public OrderSummaryController(OrderSummaryQueryService orderSummaryQueryService) {
        this.orderSummaryQueryService = orderSummaryQueryService;
    }

    /**
     * 최근 주문 프로젝션 또는 고객별 주문 프로젝션을 조회한다.
     *
     * @param customerId 고객 식별자
     * @return 주문 요약 목록
     */
    @GetMapping
    public List<OrderSummaryResponse> getRecent(@RequestParam(required = false) String customerId) {
        if (customerId != null && !customerId.isBlank()) {
            return orderSummaryQueryService.getByCustomerId(customerId);
        }
        return orderSummaryQueryService.getRecent();
    }

    /**
     * 주문 요약 프로젝션 단건을 조회한다.
     *
     * @param orderId 주문 식별자
     * @return 주문 요약 응답
     */
    @GetMapping("/{orderId}")
    public OrderSummaryResponse getById(@PathVariable String orderId) {
        return orderSummaryQueryService.getById(orderId);
    }
}
