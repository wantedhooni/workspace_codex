package com.example.securities.information;

import java.time.LocalDateTime;

public class InformationDtos {

    public record InformationEventResponse(
            String id,
            String eventType,
            String aggregateId,
            String payload,
            LocalDateTime confirmedAt
    ) {
    }
}
