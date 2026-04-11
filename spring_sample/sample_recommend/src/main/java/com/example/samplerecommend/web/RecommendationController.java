package com.example.samplerecommend.web;

import com.example.samplerecommend.dto.RecommendationResponse;
import com.example.samplerecommend.service.RecommendationService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 추천 조회 API를 제공한다.
 */
@Validated
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * 고객 기준 추천 결과를 반환한다.
     */
    @GetMapping
    public RecommendationResponse recommend(
            @RequestParam @NotBlank(message = "customerId는 필수입니다.") String customerId,
            @RequestParam(defaultValue = "3") @Min(value = 1, message = "limit은 1 이상이어야 합니다.")
            @Max(value = 10, message = "limit은 10 이하여야 합니다.") int limit) {
        return recommendationService.recommend(customerId, limit);
    }
}

