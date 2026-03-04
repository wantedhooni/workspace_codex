package com.example.samplegatewayobservability.gateway;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/backend")
public class DownstreamMockController {

    @GetMapping("/trades/{tradeId}")
    public Map<String, Object> trade(@PathVariable String tradeId) {
        return Map.of(
                "tradeId", tradeId,
                "status", "READY",
                "market", "NASDAQ"
        );
    }

    @GetMapping("/risk/{accountId}")
    public Map<String, Object> risk(@PathVariable String accountId) {
        return Map.of(
                "accountId", accountId,
                "riskLevel", "MEDIUM",
                "exposure", 61500
        );
    }
}
