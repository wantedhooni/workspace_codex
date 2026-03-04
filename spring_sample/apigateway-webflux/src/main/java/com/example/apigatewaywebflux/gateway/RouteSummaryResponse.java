package com.example.apigatewaywebflux.gateway;

public record RouteSummaryResponse(
        String routeId,
        String pathPattern,
        String description
) {
}
