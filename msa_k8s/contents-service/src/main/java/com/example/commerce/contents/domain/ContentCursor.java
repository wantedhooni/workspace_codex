package com.example.commerce.contents.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * 콘텐츠 커서 페이지네이션의 마지막 정렬 키를 보관한다.
 *
 * @param createdAt 마지막 콘텐츠 생성 시각
 * @param id 마지막 콘텐츠 식별자
 */
public record ContentCursor(Instant createdAt, UUID id) {
}
