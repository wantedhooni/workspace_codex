package com.example.securities.external;

import com.example.securities.external.ExternalDtos.ExternalMessageResponse;
import com.example.securities.external.ExternalDtos.SendMessageRequest;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/external/messages")
public class ExternalController {

    private final ExternalService externalService;

    public ExternalController(ExternalService externalService) {
        this.externalService = externalService;
    }

    @PreAuthorize("hasRole('OPERATOR')")
    @PostMapping
    public ExternalMessageResponse send(@RequestBody SendMessageRequest request) {
        return externalService.send(request);
    }

    @PreAuthorize("hasRole('OPERATOR')")
    @PostMapping("/{messageId}/retry")
    public ExternalMessageResponse retry(@PathVariable String messageId) {
        return externalService.retry(messageId);
    }

    @PreAuthorize("hasAnyRole('VIEWER','OPERATOR')")
    @GetMapping
    public List<ExternalMessageResponse> list() {
        return externalService.list();
    }
}
