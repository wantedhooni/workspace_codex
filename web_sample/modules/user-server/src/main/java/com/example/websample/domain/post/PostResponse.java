package com.example.websample.domain.post;

import java.time.Instant;

/** 게시글 조회 결과를 클라이언트에 반환하는 DTO입니다. */
public record PostResponse(
        Long id,
        String title,
        String content,
        AuthorSummary author,
        Instant createdAt,
        Instant updatedAt
) {
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                new AuthorSummary(post.getAuthor().getId(), post.getAuthor().getEmail(), post.getAuthor().getName()),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    public record AuthorSummary(Long id, String email, String name) {
    }
}
