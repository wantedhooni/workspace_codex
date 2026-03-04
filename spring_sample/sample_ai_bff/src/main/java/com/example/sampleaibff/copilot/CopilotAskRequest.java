package com.example.sampleaibff.copilot;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CopilotAskRequest(
        @NotBlank String userId,
        @NotBlank String question,
        List<String> tags
) {
    public List<String> normalizedTags() {
        return tags == null ? List.of() : tags;
    }
}
