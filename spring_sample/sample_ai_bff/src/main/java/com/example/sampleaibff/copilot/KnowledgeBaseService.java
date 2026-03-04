package com.example.sampleaibff.copilot;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeBaseService {

    private final List<KnowledgeDocument> documents = List.of(
            new KnowledgeDocument(
                    "KB-001",
                    "해외주식 주문 장애 대응 가이드",
                    "주문 장애 발생 시 주문 채널, OMS, 외부 거래소 연결 상태를 순서대로 점검하고, 거래중단 공지와 수동 복구 절차를 병행한다.",
                    List.of("incident", "ops", "trade")
            ),
            new KnowledgeDocument(
                    "KB-002",
                    "정기 배포 체크리스트",
                    "배포 전 기능 플래그 확인, 배치 중지 여부 점검, 데이터 마이그레이션 검증, 배포 후 헬스 체크와 핵심 거래 검증을 수행한다.",
                    List.of("release", "ops", "deploy")
            ),
            new KnowledgeDocument(
                    "KB-003",
                    "고객 계좌 한도 변경 승인 절차",
                    "계좌 한도 상향은 리스크 승인, 고객 통지, 변경 이력 저장이 필요하며 승인 전후 감사 로그를 반드시 남긴다.",
                    List.of("risk", "account", "approval")
            ),
            new KnowledgeDocument(
                    "KB-004",
                    "사고 보고 템플릿",
                    "사고 보고에는 영향 범위, 탐지 시각, 조치 내역, 재발 방지 계획을 포함해야 한다.",
                    List.of("incident", "report", "ops")
            ),
            new KnowledgeDocument(
                    "KB-005",
                    "해외파생 포지션 마감 운영 메모",
                    "마감 전 포지션 불일치 여부를 점검하고, 거래소별 컷오프 시간과 배치 마감 시간을 대조한다.",
                    List.of("ops", "settlement", "trade")
            )
    );

    public List<KnowledgeDocument> allDocuments() {
        return documents;
    }

    public List<KnowledgeDocument> search(String question, List<String> tags, int limit) {
        Set<String> keywords = tokenize(question, tags);

        return documents.stream()
                .map(document -> new RankedDocument(document, score(document, keywords)))
                .filter(ranked -> ranked.score() > 0)
                .sorted(Comparator.comparingInt(RankedDocument::score).reversed())
                .limit(limit)
                .map(RankedDocument::document)
                .toList();
    }

    private Set<String> tokenize(String question, List<String> tags) {
        return (question + " " + String.join(" ", tags)).toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9가-힣 ]", " ")
                .lines()
                .flatMap(line -> List.of(line.split("\\s+")).stream())
                .filter(token -> !token.isBlank())
                .collect(Collectors.toSet());
    }

    private int score(KnowledgeDocument document, Set<String> keywords) {
        int score = 0;
        String normalizedTitle = document.title().toLowerCase(Locale.ROOT);
        String normalizedContent = document.content().toLowerCase(Locale.ROOT);

        for (String keyword : keywords) {
            if (normalizedTitle.contains(keyword)) {
                score += 5;
            }
            if (normalizedContent.contains(keyword)) {
                score += 3;
            }
            if (document.tags().contains(keyword)) {
                score += 7;
            }
        }
        return score;
    }

    private record RankedDocument(KnowledgeDocument document, int score) {
    }
}
