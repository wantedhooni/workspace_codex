package com.example.apigatewaywebflux.gateway;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/routes")
public class GatewayAdminController {

    @GetMapping
    public List<RouteSummaryResponse> getRoutes() {
        return List.of(
                new RouteSummaryResponse("customer-service", "/api/customer-service/**", "고객 세그먼트 조회"),
                new RouteSummaryResponse("order-service", "/api/order-service/**", "주문 상태 조회")
        );
    }
}
