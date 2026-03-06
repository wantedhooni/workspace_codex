package com.example.sampleaibot.chat.config;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.ollama")
public record OllamaProperties(
    @NotBlank String baseUrl,
    @NotBlank String model,
    @NotBlank String systemPrompt,
    @Min(1) @Max(100) int maxHistory,
    @DecimalMin("0.0") @DecimalMax("2.0") double temperature,
    @Min(16) @Max(4096) int numPredict,
    @NotBlank String keepAlive,
    @Min(10) @Max(900) int requestTimeoutSeconds
) {
}
