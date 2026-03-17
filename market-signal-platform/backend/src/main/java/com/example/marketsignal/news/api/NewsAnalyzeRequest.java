package com.example.marketsignal.news;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 뉴스 분석 요청 모델이다.
 */
public record NewsAnalyzeRequest(
        @NotBlank(message = "헤드라인은 필수입니다.")
        @Size(max = 200, message = "헤드라인은 200자 이하여야 합니다.")
        String headline,
        @NotBlank(message = "본문은 필수입니다.")
        String content
) {
}
