package com.example.sampleaibff.copilot;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class CopilotService {

    private final KnowledgeBaseService knowledgeBaseService;

    public CopilotService(KnowledgeBaseService knowledgeBaseService) {
        this.knowledgeBaseService = knowledgeBaseService;
    }

    public CopilotResponse ask(CopilotAskRequest request) {
        List<KnowledgeDocument> documents = knowledgeBaseService.search(request.question(), request.normalizedTags(), 3);
        List<CopilotReference> references = documents.stream()
                .map(document -> new CopilotReference(
                        document.id(),
                        document.title(),
                        excerpt(document.content())
                ))
                .toList();

        String answer = documents.isEmpty()
                ? "현재 등록된 문서 기준으로는 충분한 답을 찾지 못했습니다. 질문 범위를 좁히거나 관련 태그를 추가하세요."
                : buildAnswer(request.question(), documents);

        return new CopilotResponse(
                request.userId(),
                request.question(),
                answer,
                references,
                Instant.now()
        );
    }

    public List<String> streamTokens(CopilotAskRequest request) {
        return List.of(ask(request).answer().split(" "));
    }

    private String buildAnswer(String question, List<KnowledgeDocument> documents) {
        String summary = documents.stream()
                .map(document -> "- " + document.title() + ": " + excerpt(document.content()))
                .collect(Collectors.joining("\n"));

        return """
                질문 요약: %s
                추천 답변:
                %s
                다음 액션:
                - 영향 범위를 먼저 확인한다.
                - 관련 운영 문서와 승인 절차를 함께 확인한다.
                - 필요한 경우 사고 보고 또는 공지 템플릿을 바로 사용한다.
                """.formatted(question, summary);
    }

    private String excerpt(String content) {
        return content.length() <= 80 ? content : content.substring(0, 80) + "...";
    }
}
