package com.example.orderapp.adminapi;

import com.example.orderapp.common.api.HealthPayload;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminHealthController {

    @GetMapping("/health")
    public HealthPayload health() {
        return new HealthPayload("api-admin", "UP");
    }
}
