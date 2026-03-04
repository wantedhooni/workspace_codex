package com.example.samplemcpserver.mcp;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class OpsMcpTools {

    @Tool(description = "배포 전 운영 체크리스트를 반환한다.")
    public Map<String, Object> releaseChecklist(String environment) {
        return Map.of(
                "environment", environment,
                "items", List.of(
                        "기능 플래그 상태 확인",
                        "배치 정지/재개 계획 확인",
                        "핵심 거래 API 헬스체크",
                        "롤백 절차와 담당자 확인"
                ),
                "generatedAt", Instant.now().toString()
        );
    }

    @Tool(description = "사고 보고 초안을 생성한다.")
    public Map<String, Object> incidentDraft(String title, String severity, String impact) {
        return Map.of(
                "title", title,
                "severity", severity,
                "summary", "영향도 " + severity + " 수준의 사고가 발생했으며 영향 범위는 " + impact + " 이다.",
                "sections", List.of("현상", "영향 범위", "탐지 시각", "조치 내역", "재발 방지 계획"),
                "generatedAt", Instant.now().toString()
        );
    }
}
