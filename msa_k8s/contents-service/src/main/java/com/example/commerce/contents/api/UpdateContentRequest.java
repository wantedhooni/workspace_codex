package com.example.commerce.contents.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * 콘텐츠 제목과 본문 수정 요청을 정의한다.
 *
 * @param title 변경 제목
 * @param body 변경 본문
 * @param version 클라이언트가 조회한 콘텐츠 버전
 */
public record UpdateContentRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
        String title,

        @NotBlank(message = "본문은 필수입니다.")
        @Size(max = 100000, message = "본문은 100,000자 이하여야 합니다.")
        String body,

        @PositiveOrZero(message = "버전은 0 이상이어야 합니다.")
        long version
) {
}
