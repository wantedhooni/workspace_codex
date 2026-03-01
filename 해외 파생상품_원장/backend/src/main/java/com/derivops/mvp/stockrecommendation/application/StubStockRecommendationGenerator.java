package com.derivops.mvp.stockrecommendation.application;

import com.derivops.mvp.stockrecommendation.RecommendationAction;
import com.derivops.mvp.stockrecommendation.RecommendationConfidence;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@ConditionalOnProperty(prefix = "app.ai.stock-recommendation", name = "provider", havingValue = "stub")
public class StubStockRecommendationGenerator implements StockRecommendationGenerator {

    private final StockRecommendationProperties properties;

    @Override
    public GeneratedRecommendation generate(StockRecommendationPromptContext context) {
        List<String> universe = new ArrayList<>(context.candidateSymbols());
        if (universe.isEmpty()) {
            universe.addAll(List.of("MSFT", "AMD", "QQQ", "AVGO", "AMZN"));
        }

        List<RecommendationItem> items = new ArrayList<>();
        int limit = Math.min(context.maxRecommendations(), universe.size());
        for (int index = 0; index < limit; index++) {
            String symbol = universe.get(index);
            String market = context.preferredMarkets().isEmpty() ? "NASDAQ" : context.preferredMarkets().get(0);
            RecommendationAction action = index == 0 ? RecommendationAction.BUY : RecommendationAction.WATCH;
            RecommendationConfidence confidence = index == 0 ? RecommendationConfidence.MEDIUM : RecommendationConfidence.LOW;
            items.add(new RecommendationItem(
                    index + 1,
                    symbol,
                    market,
                    action,
                    confidence,
                    "%s형 기준 단일 종목 편입 비중을 보수적으로 시작".formatted(context.riskProfile().name()),
                    "%s 보유 종목과의 분산 또는 성장 노출을 보완하는 후보로 판단했습니다.".formatted(symbol),
                    "실제 주문 전 최근 변동성, 브로커 주문 가능 시장, 내부 한도 초과 여부를 확인해야 합니다."
            ));
        }

        return new GeneratedRecommendation(
                "Stub 추천기 결과입니다. 로컬 테스트에서는 포트폴리오와 입력 조건을 바탕으로 예시 추천만 반환합니다. 실환경에서는 Ollama 응답으로 대체됩니다.",
                List.of(
                        "실제 투자 판단 전 시세, 유동성, 내부 승인 정책을 반드시 재확인해야 합니다.",
                        "현재 설정된 모델은 %s 입니다.".formatted(properties.modelName())
                ),
                items
        );
    }
}
