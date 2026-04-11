package com.example.samplerecommend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 고객 행동 적재 요청을 표현한다.
 */
public record CustomerActionRequest(
        @NotBlank(message = "상품 코드는 필수입니다.")
        String productCode,
        @NotBlank(message = "행동 타입은 필수입니다.")
        String actionType,
        @Min(value = 1, message = "가중치는 1 이상이어야 합니다.")
        @Max(value = 10, message = "가중치는 10 이하여야 합니다.")
        int weight
) {
}

