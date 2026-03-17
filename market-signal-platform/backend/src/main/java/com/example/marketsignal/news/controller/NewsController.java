package com.example.marketsignal.news;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 뉴스 분석 API를 제공한다.
 */
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@Validated
public class NewsController {

    private final NewsAnalysisService newsAnalysisService;

    @PostMapping("/analyze")
    public NewsAnalysisResponse analyze(@Valid @RequestBody NewsAnalyzeRequest request) {
        return newsAnalysisService.analyze(request);
    }

    @GetMapping("/analyses")
    public List<NewsAnalysisResponse> getRecentAnalyses(
            @RequestParam(defaultValue = "6")
            @Min(value = 1, message = "조회 개수는 1 이상이어야 합니다.")
            @Max(value = 10, message = "조회 개수는 10 이하여야 합니다.")
            int limit,
            @RequestParam(required = false)
            String query,
            @RequestParam(required = false)
            NewsSentiment sentiment
    ) {
        return newsAnalysisService.getRecentAnalyses(limit, query, sentiment);
    }
}
