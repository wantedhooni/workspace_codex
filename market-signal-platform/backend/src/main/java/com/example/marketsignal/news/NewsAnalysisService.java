package com.example.marketsignal.news;

import com.example.marketsignal.ai.AiNarrativeService;
import java.util.Locale;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 뉴스 텍스트를 감성 및 영향도로 분석한다.
 */
@Service
@RequiredArgsConstructor
public class NewsAnalysisService {

    private static final Set<String> POSITIVE_KEYWORDS = Set.of(
            "beat", "upgrade", "growth", "record", "surge", "strong", "partnership", "expansion", "profit"
    );
    private static final Set<String> NEGATIVE_KEYWORDS = Set.of(
            "miss", "downgrade", "lawsuit", "drop", "weak", "cut", "investigation", "decline", "loss"
    );

    private final NewsAnalysisRepository newsAnalysisRepository;
    private final AiNarrativeService aiNarrativeService;

    /**
     * 헤드라인과 본문을 기반으로 뉴스 분석 결과를 생성한다.
     */
    @Transactional
    public NewsAnalysisResponse analyze(NewsAnalyzeRequest request) {
        String text = (request.headline() + " " + request.content()).toLowerCase(Locale.ENGLISH);
        long positiveHits = POSITIVE_KEYWORDS.stream().filter(text::contains).count();
        long negativeHits = NEGATIVE_KEYWORDS.stream().filter(text::contains).count();

        NewsSentiment sentiment = positiveHits > negativeHits
                ? NewsSentiment.POSITIVE
                : negativeHits > positiveHits ? NewsSentiment.NEGATIVE : NewsSentiment.NEUTRAL;

        String impact = determineImpact(request.content(), sentiment, positiveHits, negativeHits);
        String summary = request.content().length() > 140
                ? request.content().substring(0, 140) + "..."
                : request.content();
        String interpretation = aiNarrativeService.generateNewsInterpretation(sentiment, impact);

        NewsAnalysis analysis = NewsAnalysis.builder()
                .headline(request.headline())
                .content(request.content())
                .sentiment(sentiment)
                .summary(summary)
                .impact(impact)
                .interpretation(interpretation)
                .build();

        return NewsAnalysisResponse.from(newsAnalysisRepository.save(analysis));
    }

    /**
     * 최근 생성된 뉴스 분석 이력을 조회한다.
     */
    @Transactional(readOnly = true)
    public List<NewsAnalysisResponse> getRecentAnalyses(int limit, String query, NewsSentiment sentiment) {
        int normalizedLimit = Math.min(Math.max(limit, 1), 10);
        String normalizedQuery = query == null ? null : query.trim();
        return newsAnalysisRepository.searchRecent(normalizedQuery, sentiment, PageRequest.of(0, normalizedLimit)).stream()
                .map(NewsAnalysisResponse::from)
                .toList();
    }

    private String determineImpact(String content, NewsSentiment sentiment, long positiveHits, long negativeHits) {
        int lengthScore = content.length() > 500 ? 2 : 1;
        long sentimentHits = Math.max(positiveHits, negativeHits);
        int totalScore = lengthScore + (int) sentimentHits;
        if (sentiment == NewsSentiment.NEUTRAL && totalScore <= 2) {
            return "LOW";
        }
        if (totalScore >= 4) {
            return "HIGH";
        }
        return "MEDIUM";
    }
}
