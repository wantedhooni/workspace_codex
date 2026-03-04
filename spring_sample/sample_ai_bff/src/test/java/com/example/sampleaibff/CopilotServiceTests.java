package com.example.sampleaibff;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.sampleaibff.copilot.CopilotAskRequest;
import com.example.sampleaibff.copilot.CopilotService;
import com.example.sampleaibff.copilot.KnowledgeBaseService;
import java.util.List;
import org.junit.jupiter.api.Test;

class CopilotServiceTests {

    @Test
    void returnsReferencesForRelevantQuestion() {
        CopilotService service = new CopilotService(new KnowledgeBaseService());

        var response = service.ask(new CopilotAskRequest("ops01", "주문 장애 대응 절차를 알려줘", List.of("incident")));

        assertThat(response.references()).isNotEmpty();
        assertThat(response.answer()).contains("질문 요약");
    }
}
