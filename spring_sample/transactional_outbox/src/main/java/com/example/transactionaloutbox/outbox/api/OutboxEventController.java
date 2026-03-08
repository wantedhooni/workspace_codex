package com.example.transactionaloutbox.outbox.api;

import com.example.transactionaloutbox.outbox.application.OutboxQueryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/outbox-events")
public class OutboxEventController {

    private final OutboxQueryService outboxQueryService;

    public OutboxEventController(OutboxQueryService outboxQueryService) {
        this.outboxQueryService = outboxQueryService;
    }

    @GetMapping
    public List<OutboxEventResponse> getRecentEvents() {
        return outboxQueryService.getRecentEvents();
    }
}
