package com.derivops.mvp.stockrecommendation.dto;

import com.derivops.mvp.stockrecommendation.RecommendationHorizon;
import com.derivops.mvp.stockrecommendation.RecommendationRiskProfile;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record GenerateStockRecommendationRequest(
        @NotNull Long accountId,
        @NotNull RecommendationRiskProfile riskProfile,
        @NotNull RecommendationHorizon investmentHorizon,
        @Min(1) @Max(5) Integer maxRecommendations,
        List<String> preferredMarkets,
        List<String> candidateSymbols,
        String operatorView
) {
}
