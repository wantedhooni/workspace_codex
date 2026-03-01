package com.derivops.mvp.stockrecommendation.application;

public interface StockRecommendationGenerator {

    GeneratedRecommendation generate(StockRecommendationPromptContext context);

    record GeneratedRecommendation(
            String summary,
            java.util.List<String> cautionPoints,
            java.util.List<RecommendationItem> recommendations
    ) {
    }

    record RecommendationItem(
            int rank,
            String symbol,
            String market,
            com.derivops.mvp.stockrecommendation.RecommendationAction action,
            com.derivops.mvp.stockrecommendation.RecommendationConfidence confidence,
            String allocationHint,
            String rationale,
            String riskNotes
    ) {
    }
}
