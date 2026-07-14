package com.example.commerce.contents.api;

import java.time.Instant;
import java.util.UUID;

import com.example.commerce.contents.domain.Content;
import com.example.commerce.contents.domain.ContentStatus;
import com.example.commerce.contents.domain.ContentType;

/**
 * 금융 콘텐츠 API의 응답 형식을 정의한다.
 *
 * @param id 콘텐츠 식별자
 * @param type 콘텐츠 유형
 * @param title 제목
 * @param body 본문
 * @param authorId 작성 회원 식별자
 * @param status 공개 상태
 * @param version 콘텐츠 변경 버전
 * @param publishedAt 최초 발행 시각
 * @param createdAt 생성 시각
 * @param updatedAt 최종 변경 시각
 */
public record ContentResponse(
        UUID id,
        ContentType type,
        String title,
        String body,
        UUID authorId,
        ContentStatus status,
        long version,
        Instant publishedAt,
        Instant createdAt,
        Instant updatedAt
) {

    /**
     * 콘텐츠 도메인 모델을 API 응답으로 변환한다.
     *
     * @param content 변환할 콘텐츠
     * @return 콘텐츠 응답
     */
    public static ContentResponse from(Content content) {
        return new ContentResponse(
                content.id(),
                content.type(),
                content.title(),
                content.body(),
                content.authorId(),
                content.status(),
                content.version(),
                content.publishedAt(),
                content.createdAt(),
                content.updatedAt());
    }
}
