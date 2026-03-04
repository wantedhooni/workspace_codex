package com.example.samplemcpclient.mcp;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record McpCallRequest(
        @NotBlank String toolName,
        Map<String, Object> arguments
) {
    public Map<String, Object> normalizedArguments() {
        return arguments == null ? Map.of() : arguments;
    }
}
