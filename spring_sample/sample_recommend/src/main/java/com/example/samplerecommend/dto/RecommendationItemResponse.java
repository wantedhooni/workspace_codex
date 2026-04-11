package com.example.samplerecommend.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * 개별 추천 상품 응답을 표현한다.
 */
public record RecommendationItemResponse(
        String productCode,
        String productName,
        String category,
        BigDecimal price,
        double score,
        List<String> reasons
) {
}

