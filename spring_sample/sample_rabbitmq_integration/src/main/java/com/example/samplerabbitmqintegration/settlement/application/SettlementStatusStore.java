package com.example.samplerabbitmqintegration.settlement.application;

import com.example.samplerabbitmqintegration.settlement.api.SettlementStatusResponse;
import com.example.samplerabbitmqintegration.settlement.domain.SettlementMessage;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class SettlementStatusStore {

    private final Map<String, SettlementStatusResponse> statuses = new ConcurrentHashMap<>();

    public void markPublished(SettlementMessage message) {
        statuses.put(message.settlementId(), toResponse(message, "PUBLISHED", "RabbitMQ exchange 발행 완료"));
    }

    public void markProcessed(SettlementMessage message) {
        statuses.put(message.settlementId(), toResponse(message, "PROCESSED", "정산 요청 처리 완료"));
    }

    public void markFailed(SettlementMessage message, String detail) {
        statuses.put(message.settlementId(), toResponse(message, "FAILED", detail));
    }

    public SettlementStatusResponse get(String settlementId) {
        return statuses.get(settlementId);
    }

    private SettlementStatusResponse toResponse(SettlementMessage message, String status, String detail) {
        return new SettlementStatusResponse(
                message.settlementId(),
                message.bookCode(),
                message.currency(),
                message.amount(),
                message.counterparty(),
                status,
                detail,
                Instant.now()
        );
    }
}
