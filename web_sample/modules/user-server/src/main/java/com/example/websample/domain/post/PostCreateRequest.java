package com.example.websample.domain.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 게시글 생성 요청 값을 담는 DTO입니다. */
public record PostCreateRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank String content
) {
}
