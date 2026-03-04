package com.example.samplesecurebff.downstream;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface RiskServiceClient {

    @GetExchange("/internal/risk/{accountId}")
    RiskSnapshot getRisk(@PathVariable String accountId);
}
