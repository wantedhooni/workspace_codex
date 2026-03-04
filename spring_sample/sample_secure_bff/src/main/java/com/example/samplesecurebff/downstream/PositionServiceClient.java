package com.example.samplesecurebff.downstream;

import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface PositionServiceClient {

    @GetExchange("/internal/positions/{accountId}")
    List<PositionView> getPositions(@PathVariable String accountId);
}
