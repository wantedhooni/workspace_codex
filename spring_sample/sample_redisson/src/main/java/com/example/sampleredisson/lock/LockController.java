package com.example.sampleredisson.lock;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/locks")
public class LockController {

    private final LockDemoService lockDemoService;

    public LockController(LockDemoService lockDemoService) {
        this.lockDemoService = lockDemoService;
    }

    @PostMapping("/{name}/execute")
    public LockExecutionResponse execute(
            @PathVariable String name,
            @Valid @RequestBody LockExecutionRequest request
    ) {
        return lockDemoService.execute(name, request);
    }
}
