package com.example.samplekafkaintegration.messaging.api;

import com.example.samplekafkaintegration.messaging.application.TradeInstructionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TradeInstructionController {

    private final TradeInstructionService tradeInstructionService;

    public TradeInstructionController(TradeInstructionService tradeInstructionService) {
        this.tradeInstructionService = tradeInstructionService;
    }

    @PostMapping("/api/trade-instructions")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TradeInstructionStatusResponse publish(@Valid @RequestBody TradeInstructionRequest request) {
        return tradeInstructionService.publish(request);
    }

    @GetMapping("/api/trade-instructions/{messageId}")
    public TradeInstructionStatusResponse getStatus(@PathVariable String messageId) {
        return tradeInstructionService.getStatus(messageId);
    }
}
