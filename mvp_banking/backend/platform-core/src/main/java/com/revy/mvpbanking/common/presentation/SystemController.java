package com.revy.mvpbanking.common.presentation;

import com.revy.mvpbanking.common.api.ApiResponse;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    @GetMapping("/ping")
    public ApiResponse<Map<String, Object>> ping() {
        return ApiResponse.ok(Map.of(
                "service", "mvp-banking-backend",
                "status", "UP",
                "timestamp", OffsetDateTime.now(ZoneOffset.UTC).toString()
        ));
    }
}
