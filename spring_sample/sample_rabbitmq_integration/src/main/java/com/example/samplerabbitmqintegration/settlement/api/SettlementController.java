package com.example.samplerabbitmqintegration.settlement.api;

import com.example.samplerabbitmqintegration.settlement.application.SettlementDispatchService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SettlementController {

    private final SettlementDispatchService dispatchService;

    public SettlementController(SettlementDispatchService dispatchService) {
        this.dispatchService = dispatchService;
    }

    @PostMapping("/api/settlements")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public SettlementStatusResponse dispatch(@Valid @RequestBody DispatchSettlementRequest request) {
        return dispatchService.dispatch(request);
    }

    @GetMapping("/api/settlements/{settlementId}")
    public SettlementStatusResponse get(@PathVariable String settlementId) {
        return dispatchService.getStatus(settlementId);
    }
}
