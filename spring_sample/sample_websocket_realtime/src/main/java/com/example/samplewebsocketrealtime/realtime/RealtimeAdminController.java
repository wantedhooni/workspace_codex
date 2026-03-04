package com.example.samplewebsocketrealtime.realtime;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/realtime")
public class RealtimeAdminController {

    private final RealtimePublisherService realtimePublisherService;

    public RealtimeAdminController(RealtimePublisherService realtimePublisherService) {
        this.realtimePublisherService = realtimePublisherService;
    }

    @PostMapping("/prices")
    public PriceUpdate price(@Valid @RequestBody PriceUpdateRequest request) {
        return realtimePublisherService.publishPrice(request);
    }

    @PostMapping("/jobs")
    public JobProgressUpdate job(@Valid @RequestBody JobProgressRequest request) {
        return realtimePublisherService.publishJob(request);
    }
}
