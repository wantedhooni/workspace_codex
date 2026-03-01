package com.derivops.mvp.stockrecommendation.application;

import com.derivops.mvp.common.ServiceUnavailableException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.ai.chat.client.ChatClient;

@RequiredArgsConstructor
@Component
@ConditionalOnProperty(prefix = "app.ai.stock-recommendation", name = "provider", havingValue = "ollama", matchIfMissing = true)
public class OllamaStockRecommendationGenerator implements StockRecommendationGenerator {

    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;
    private final ObjectMapper objectMapper;
    private final StockRecommendationProperties properties;

    @Override
    public GeneratedRecommendation generate(StockRecommendationPromptContext context) {
        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        if (builder == null) {
            throw new ServiceUnavailableException("Spring AI Ollama client is not configured");
        }

        String content;
        try {
            content = builder.build()
                    .prompt()
                    .system(systemPrompt())
                    .user(userPrompt(context))
                    .call()
                    .content();
        } catch (Exception ex) {
            throw new ServiceUnavailableException("Ollama recommendation request failed: " + ex.getMessage());
        }

        if (!StringUtils.hasText(content)) {
            throw new ServiceUnavailableException("Ollama returned an empty recommendation response");
        }

        try {
            return objectMapper.readValue(content, GeneratedRecommendation.class);
        } catch (JsonProcessingException ex) {
            throw new ServiceUnavailableException("Ollama returned an invalid recommendation payload");
        }
    }

    private String systemPrompt() {
        return """
                You are a cautious securities operations assistant.
                Generate stock recommendation drafts for an internal operations dashboard.
                The output must be valid JSON only.
                Do not include markdown fences or any prose outside JSON.
                Recommendation actions must be BUY, WATCH, or HOLD.
                Confidence must be LOW, MEDIUM, or HIGH.
                Keep the answer grounded in the provided portfolio context and operator constraints.
                Mention risks clearly and avoid claiming certainty.
                """;
    }

    private String userPrompt(StockRecommendationPromptContext context) {
        return """
                Return JSON with this schema:
                {
                  "summary": "string",
                  "cautionPoints": ["string"],
                  "recommendations": [
                    {
                      "rank": 1,
                      "symbol": "string",
                      "market": "string",
                      "action": "BUY|WATCH|HOLD",
                      "confidence": "LOW|MEDIUM|HIGH",
                      "allocationHint": "string",
                      "rationale": "string",
                      "riskNotes": "string"
                    }
                  ]
                }

                Constraints:
                - Return at most %d recommendations.
                - If candidate symbols are provided, prioritize them.
                - Use Korean for summary, allocationHint, rationale, and riskNotes.
                - Avoid promising returns. Mention portfolio concentration, cash usage, and operational checks where relevant.

                Account context:
                - accountId: %d
                - accountNo: %s
                - broker: %s
                - accountStatus: %s
                - ownerName: %s
                - cashSnapshotDate: %s
                - riskProfile: %s
                - investmentHorizon: %s
                - preferredMarkets: %s
                - candidateSymbols: %s
                - operatorNote: %s

                Cash balances:
                %s

                Current holdings:
                %s

                Recent purchases:
                %s
                """.formatted(
                context.maxRecommendations(),
                context.accountId(),
                context.accountNo(),
                context.broker(),
                context.accountStatus(),
                context.ownerName(),
                context.cashSnapshotDate(),
                context.riskProfile(),
                context.investmentHorizon(),
                join(context.preferredMarkets()),
                join(context.candidateSymbols()),
                blankToDash(context.operatorNote()),
                formatCash(context),
                formatHoldings(context),
                formatRecentPurchases(context)
        );
    }

    private String join(List<String> items) {
        return items == null || items.isEmpty() ? "-" : String.join(", ", items);
    }

    private String blankToDash(String value) {
        return StringUtils.hasText(value) ? value.trim() : "-";
    }

    private String formatCash(StockRecommendationPromptContext context) {
        if (context.cashBalances().isEmpty()) {
            return "-";
        }
        return context.cashBalances().stream()
                .map(item -> "%s %s".formatted(item.currency(), item.amount()))
                .collect(Collectors.joining("\n"));
    }

    private String formatHoldings(StockRecommendationPromptContext context) {
        if (context.holdings().isEmpty()) {
            return "-";
        }
        return context.holdings().stream()
                .map(item -> "%s %s qty=%s avg=%s totalCost=%s lastTrade=%s".formatted(
                        item.market(),
                        item.symbol(),
                        item.quantity(),
                        item.averagePrice(),
                        item.totalCost(),
                        item.lastTradeDate()
                ))
                .collect(Collectors.joining("\n"));
    }

    private String formatRecentPurchases(StockRecommendationPromptContext context) {
        if (context.recentPurchases().isEmpty()) {
            return "-";
        }
        return context.recentPurchases().stream()
                .map(item -> "%s %s tradeDate=%s qty=%s price=%s net=%s".formatted(
                        item.market(),
                        item.symbol(),
                        item.tradeDate(),
                        item.quantity(),
                        item.price(),
                        item.netAmount()
                ))
                .collect(Collectors.joining("\n"));
    }
}
