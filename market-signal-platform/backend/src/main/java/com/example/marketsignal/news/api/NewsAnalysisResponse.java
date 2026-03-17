package com.example.marketsignal.news;

import java.time.LocalDateTime;

/**
 * 뉴스 분석 응답 모델이다.
 */
public record NewsAnalysisResponse(
        Long id,
        String headline,
        NewsSentiment sentiment,
        String summary,
        String impact,
        String interpretation,
        LocalDateTime analyzedAt
) {

    public static NewsAnalysisResponse from(NewsAnalysis analysis) {
        return new NewsAnalysisResponse(
                analysis.getId(),
                analysis.getHeadline(),
                analysis.getSentiment(),
                analysis.getSummary(),
                analysis.getImpact(),
                analysis.getInterpretation(),
                analysis.getCreatedAt()
        );
    }
}
