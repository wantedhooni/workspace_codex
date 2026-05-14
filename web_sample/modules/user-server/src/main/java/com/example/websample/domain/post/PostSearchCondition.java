package com.example.websample.domain.post;

/** 게시글 목록 검색에 사용할 조건을 담는 DTO입니다. */
public record PostSearchCondition(
        String keyword,
        Long authorId
) {
}
