package com.example.samplemcpclient;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.samplemcpclient.mcp.McpCallRequest;
import java.util.Map;
import org.junit.jupiter.api.Test;

class McpCallRequestTests {

    @Test
    void normalizesNullArguments() {
        McpCallRequest request = new McpCallRequest("releaseChecklist", null);

        assertThat(request.normalizedArguments()).isEqualTo(Map.of());
    }
}
