package com.derivops.mvp.stockrecommendation.api;

import com.derivops.mvp.stockrecommendation.application.StockRecommendationService;
import com.derivops.mvp.stockrecommendation.dto.GenerateStockRecommendationRequest;
import com.derivops.mvp.stockrecommendation.dto.StockRecommendationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/stock-recommendations")
public class StockRecommendationController {

    private final StockRecommendationService stockRecommendationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OPS_ADMIN','OPS_VIEWER')")
    public StockRecommendationResponse generate(@Valid @RequestBody GenerateStockRecommendationRequest request) {
        return stockRecommendationService.recommend(request);
    }
}
