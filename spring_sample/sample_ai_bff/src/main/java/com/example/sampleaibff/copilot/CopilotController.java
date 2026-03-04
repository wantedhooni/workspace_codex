package com.example.sampleaibff.copilot;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/copilot")
public class CopilotController {

    private final CopilotService copilotService;
    private final KnowledgeBaseService knowledgeBaseService;

    public CopilotController(CopilotService copilotService, KnowledgeBaseService knowledgeBaseService) {
        this.copilotService = copilotService;
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @PostMapping("/ask")
    public CopilotResponse ask(@Valid @RequestBody CopilotAskRequest request) {
        return copilotService.ask(request);
    }

    @PostMapping(path = "/ask/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter askStream(@Valid @RequestBody CopilotAskRequest request) {
        SseEmitter emitter = new SseEmitter(0L);

        CompletableFuture.runAsync(() -> {
            try {
                for (String token : copilotService.streamTokens(request)) {
                    emitter.send(SseEmitter.event().name("token").data(token));
                    Thread.sleep(40L);
                }
                emitter.send(SseEmitter.event().name("complete").data("done"));
                emitter.complete();
            } catch (IOException exception) {
                emitter.completeWithError(exception);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                emitter.completeWithError(exception);
            }
        });

        return emitter;
    }

    @GetMapping("/knowledge")
    public List<KnowledgeDocument> knowledge() {
        return knowledgeBaseService.allDocuments();
    }
}
