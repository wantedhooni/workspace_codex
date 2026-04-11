package com.example.samplerecommend.dto;

import java.time.LocalDateTime;

/**
 * 고객 행동 적재 결과를 표현한다.
 */
public record CustomerActionResponse(
        Long id,
        String customerId,
        String productCode,
        String actionType,
        int weight,
        LocalDateTime actedAt
) {
}

