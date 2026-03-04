package com.example.apigatewaywebflux.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {

    @Bean
    RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("customer-service", route -> route
                        .path("/api/customer-service/customers/{customerCode}")
                        .filters(filter -> filter.setPath("/mock/customer-service/customers/{customerCode}"))
                        .uri("forward:/"))
                .route("order-service", route -> route
                        .path("/api/order-service/orders/{orderNumber}")
                        .filters(filter -> filter.setPath("/mock/order-service/orders/{orderNumber}"))
                        .uri("forward:/"))
                .build();
    }
}
