package com.example.samplemcpclient.mcp;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class McpClientService {

    private final List<McpSyncClient> mcpSyncClients;

    public McpClientService(List<McpSyncClient> mcpSyncClients) {
        this.mcpSyncClients = mcpSyncClients;
    }

    public List<McpToolSummary> listTools() {
        McpSchema.ListToolsResult result = primaryClient().listTools();
        return result.tools().stream()
                .map(tool -> new McpToolSummary(tool.name(), tool.description()))
                .toList();
    }

    public Object callTool(McpCallRequest request) {
        McpSchema.CallToolRequest toolRequest = new McpSchema.CallToolRequest(request.toolName(), request.normalizedArguments());
        McpSchema.CallToolResult result = primaryClient().callTool(toolRequest);
        return result.content();
    }

    public Object readResource(String resourceUri) {
        return primaryClient().readResource(new McpSchema.ReadResourceRequest(resourceUri)).contents();
    }

    private McpSyncClient primaryClient() {
        return mcpSyncClients.stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("연결된 MCP 클라이언트가 없습니다."));
    }
}
