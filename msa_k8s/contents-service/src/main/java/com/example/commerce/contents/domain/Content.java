package com.example.commerce.contents.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * 금융 공지사항 또는 게시물의 본문과 공개 상태를 나타내는 도메인 모델이다.
 *
 * @param id 콘텐츠 식별자
 * @param type 콘텐츠 유형
 * @param title 제목
 * @param body 본문
 * @param authorId 작성 회원 식별자
 * @param status 공개 상태
 * @param version 동시 변경 감지용 버전
 * @param publishedAt 최초 발행 시각
 * @param createdAt 생성 시각
 * @param updatedAt 최종 변경 시각
 */
public record Content(
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
}
