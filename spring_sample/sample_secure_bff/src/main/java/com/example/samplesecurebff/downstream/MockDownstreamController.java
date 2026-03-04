package com.example.samplesecurebff.downstream;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal")
public class MockDownstreamController {

    @GetMapping("/customers/{accountId}")
    public CustomerProfile customer(@PathVariable String accountId) {
        return new CustomerProfile(accountId, "Kim Trader", "PRO", "USD");
    }

    @GetMapping("/positions/{accountId}")
    public List<PositionView> positions(@PathVariable String accountId) {
        return List.of(
                new PositionView("AAPL", 120, new BigDecimal("23250.00")),
                new PositionView("NVDA", 40, new BigDecimal("38400.00"))
        );
    }

    @GetMapping("/risk/{accountId}")
    public RiskSnapshot risk(@PathVariable String accountId) {
        return new RiskSnapshot(accountId, new BigDecimal("61650.00"), "MEDIUM");
    }
}
