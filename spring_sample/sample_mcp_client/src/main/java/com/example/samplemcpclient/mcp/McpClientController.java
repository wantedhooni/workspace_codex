package com.example.samplemcpclient.mcp;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mcp")
public class McpClientController {

    private final McpClientService mcpClientService;

    public McpClientController(McpClientService mcpClientService) {
        this.mcpClientService = mcpClientService;
    }

    @GetMapping("/tools")
    public Object tools() {
        return mcpClientService.listTools();
    }

    @PostMapping("/tools/call")
    public Object call(@Valid @RequestBody McpCallRequest request) {
        return mcpClientService.callTool(request);
    }

    @GetMapping("/resources/{resourceUri}")
    public Object resource(@PathVariable String resourceUri) {
        return mcpClientService.readResource(resourceUri);
    }
}
