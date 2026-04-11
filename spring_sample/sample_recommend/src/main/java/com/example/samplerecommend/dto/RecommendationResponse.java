package com.example.samplerecommend.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 추천 결과 전체 응답을 표현한다.
 */
public record RecommendationResponse(
        String customerId,
        int limit,
        LocalDateTime recommendedAt,
        List<RecommendationItemResponse> items
) {
}

