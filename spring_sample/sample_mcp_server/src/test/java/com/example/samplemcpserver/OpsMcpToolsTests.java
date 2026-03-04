package com.example.samplemcpserver;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.samplemcpserver.mcp.OpsMcpTools;
import org.junit.jupiter.api.Test;

class OpsMcpToolsTests {

    @Test
    void createsIncidentDraft() {
        OpsMcpTools tools = new OpsMcpTools();

        var result = tools.incidentDraft("주문 지연", "HIGH", "주문 API 일부 지연");

        assertThat(result.get("title")).isEqualTo("주문 지연");
        assertThat(result.get("summary").toString()).contains("HIGH");
    }
}
