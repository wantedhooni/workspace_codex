package com.derivops.mvp.stockrecommendation.application;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai.stock-recommendation")
public record StockRecommendationProperties(
        boolean enabled,
        String provider,
        String modelName,
        int maxRecommendations,
        String disclaimer
) {
}
