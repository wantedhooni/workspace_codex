package com.example.sampleaibff.copilot;

import java.util.List;

public record KnowledgeDocument(
        String id,
        String title,
        String content,
        List<String> tags
) {
}
