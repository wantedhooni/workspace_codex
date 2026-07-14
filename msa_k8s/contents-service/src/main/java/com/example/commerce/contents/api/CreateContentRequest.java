package com.example.commerce.contents.api;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.example.commerce.contents.domain.ContentType;

/**
 * 금융 공지사항 또는 게시물 생성 요청을 정의한다.
 *
 * @param type 콘텐츠 유형
 * @param title 제목
 * @param body 본문
 * @param authorId 작성 회원 식별자
 */
public record CreateContentRequest(
        @NotNull(message = "콘텐츠 유형은 필수입니다.")
        ContentType type,

        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
        String title,

        @NotBlank(message = "본문은 필수입니다.")
        @Size(max = 100000, message = "본문은 100,000자 이하여야 합니다.")
        String body,

        @NotNull(message = "작성자 식별자는 필수입니다.")
        UUID authorId
) {
}
