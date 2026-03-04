package com.example.webfluxsample.operations;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/operations")
public class OperationsController {

    private final OperationsService operationsService;

    public OperationsController(OperationsService operationsService) {
        this.operationsService = operationsService;
    }

    @GetMapping("/dashboard")
    public Mono<OperationsDashboardResponse> getDashboard(@RequestParam(defaultValue = "payments") String domain) {
        return operationsService.getDashboard(domain);
    }

    @GetMapping(value = "/events/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<OperationsEventResponse>> streamEvents(
            @RequestParam(defaultValue = "payments") String domain
    ) {
        return operationsService.streamEvents(domain)
                .map(event -> ServerSentEvent.<OperationsEventResponse>builder()
                        .event("operations-event")
                        .data(event)
                        .build());
    }

    @PostMapping("/latency-check")
    public Mono<LatencyCheckResponse> checkLatency(@Valid @RequestBody LatencyCheckRequest request) {
        return operationsService.checkLatency(request);
    }
}
