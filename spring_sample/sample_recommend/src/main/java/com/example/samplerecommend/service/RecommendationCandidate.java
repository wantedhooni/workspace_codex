package com.example.samplerecommend.service;

import com.example.samplerecommend.domain.Product;
import java.util.List;

/**
 * 추천 계산 중간 결과를 표현한다.
 */
public record RecommendationCandidate(
        Product product,
        double score,
        List<String> reasons
) {
}

