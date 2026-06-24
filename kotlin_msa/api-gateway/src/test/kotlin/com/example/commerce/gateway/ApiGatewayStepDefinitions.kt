package com.example.commerce.gateway

import io.cucumber.java.ko.그러면
import io.cucumber.java.ko.만일
import io.cucumber.java.ko.조건
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.gateway.route.RouteLocator
import java.time.Duration

class ApiGatewayStepDefinitions {
    @Autowired
    private lateinit var routeLocator: RouteLocator

    private var routeIds: List<String> = emptyList()

    @조건("API Gateway 설정이 로드되어 있다")
    fun apiGatewayConfigurationIsLoaded() {
        assertThat(routeLocator).isNotNull()
    }

    @만일("Gateway 라우트 목록을 조회한다")
    fun findGatewayRoutes() {
        routeIds = routeLocator.routes
            .collectList()
            .block(Duration.ofSeconds(5))
            .orEmpty()
            .map { it.id }
    }

    @그러면("{string} 라우트가 등록되어 있다")
    fun routeShouldBeRegistered(routeId: String) {
        assertThat(routeIds).contains(routeId)
    }
}
