package com.example.transactionaloutbox.outbox.api;

import com.example.transactionaloutbox.outbox.application.OutboxQueryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Outbox 이벤트 조회 API를 제공한다.
 */
@RestController
@RequestMapping("/api/outbox-events")
public class OutboxEventController {

    private final OutboxQueryService outboxQueryService;

    public OutboxEventController(OutboxQueryService outboxQueryService) {
        this.outboxQueryService = outboxQueryService;
    }

    /**
     * 최근 Outbox 이벤트 목록을 조회한다.
     *
     * @return Outbox 이벤트 목록
     */
    @GetMapping
    public List<OutboxEventResponse> getRecentEvents() {
        return outboxQueryService.getRecentEvents();
    }
}
