package com.example.samplerecommend.dto;

import java.math.BigDecimal;

/**
 * 상품 목록 응답을 표현한다.
 */
public record ProductResponse(
        String productCode,
        String name,
        String category,
        BigDecimal price,
        int stockQuantity,
        double popularityScore
) {
}

