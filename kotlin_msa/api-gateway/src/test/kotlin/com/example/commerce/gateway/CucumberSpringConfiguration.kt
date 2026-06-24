package com.example.commerce.gateway

import io.cucumber.spring.CucumberContextConfiguration
import org.springframework.boot.test.context.SpringBootTest

@CucumberContextConfiguration
@SpringBootTest(
    properties = [
        "spring.cloud.kubernetes.enabled=false",
        "spring.cloud.kubernetes.config.enabled=false",
    ],
)
class CucumberSpringConfiguration

