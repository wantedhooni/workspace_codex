package com.example.orderapp.webapi;

import com.example.orderapp.common.api.HealthPayload;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    @GetMapping("/health")
    public HealthPayload health() {
        return new HealthPayload("web-api", "UP");
    }
}
