package com.example.sampleaibot.chat.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatRequest(
    String sessionId,
    @NotBlank(message = "message는 필수입니다.")
    @Size(max = 4000, message = "message는 4000자를 초과할 수 없습니다.")
    String message,
    @Size(max = 128, message = "model 이름 길이가 너무 깁니다.")
    String model
) {
}
