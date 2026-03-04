package com.example.samplewebsocketrealtime.realtime;

import java.time.Instant;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class RealtimePublisherService {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public RealtimePublisherService(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public PriceUpdate publishPrice(PriceUpdateRequest request) {
        PriceUpdate update = new PriceUpdate(request.instrument(), request.price(), Instant.now());
        simpMessagingTemplate.convertAndSend("/topic/prices", update);
        return update;
    }

    public JobProgressUpdate publishJob(JobProgressRequest request) {
        JobProgressUpdate update = new JobProgressUpdate(request.jobName(), request.progress(), request.status(), Instant.now());
        simpMessagingTemplate.convertAndSend("/topic/jobs", update);
        return update;
    }
}
