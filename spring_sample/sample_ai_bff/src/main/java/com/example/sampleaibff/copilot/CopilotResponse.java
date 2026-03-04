package com.example.sampleaibff.copilot;

import java.time.Instant;
import java.util.List;

public record CopilotResponse(
        String userId,
        String question,
        String answer,
        List<CopilotReference> references,
        Instant generatedAt
) {
}
