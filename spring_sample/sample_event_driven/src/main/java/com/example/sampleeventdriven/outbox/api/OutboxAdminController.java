package com.example.sampleeventdriven.outbox.api;

import com.example.sampleeventdriven.outbox.application.OutboxPublisherService;
import com.example.sampleeventdriven.outbox.domain.OutboxEventRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/outbox")
public class OutboxAdminController {

    private final OutboxPublisherService outboxPublisherService;
    private final OutboxEventRepository outboxEventRepository;

    public OutboxAdminController(OutboxPublisherService outboxPublisherService, OutboxEventRepository outboxEventRepository) {
        this.outboxPublisherService = outboxPublisherService;
        this.outboxEventRepository = outboxEventRepository;
    }

    @GetMapping("/pending")
    public OutboxPendingResponse pending() {
        return new OutboxPendingResponse(outboxEventRepository.countByPublishedFalse());
    }

    @PostMapping("/publish")
    public OutboxPublishResponse publish() {
        return new OutboxPublishResponse(outboxPublisherService.publishPendingEvents());
    }
}
