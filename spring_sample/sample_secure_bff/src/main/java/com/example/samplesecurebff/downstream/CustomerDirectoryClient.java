package com.example.samplesecurebff.downstream;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface CustomerDirectoryClient {

    @GetExchange("/internal/customers/{accountId}")
    CustomerProfile getCustomer(@PathVariable String accountId);
}
