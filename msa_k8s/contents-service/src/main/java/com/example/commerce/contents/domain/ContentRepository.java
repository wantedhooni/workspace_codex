package com.example.commerce.contents.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 콘텐츠 원본 데이터의 저장과 커서 조회를 제공하는 도메인 저장소 계약이다.
 */
public interface ContentRepository {

    /**
     * 신규 또는 변경된 콘텐츠를 저장한다.
     *
     * @param content 저장할 콘텐츠
     * @return 저장된 콘텐츠
     */
    Content save(Content content);

    /**
     * 식별자로 콘텐츠를 조회한다.
     *
     * @param id 콘텐츠 식별자
     * @return 존재할 경우 콘텐츠
     */
    Optional<Content> findById(UUID id);

    /**
     * 유형과 상태 조건에 따라 생성 역순으로 콘텐츠를 제한 조회한다.
     *
     * @param type 선택 콘텐츠 유형
     * @param status 선택 콘텐츠 상태
     * @param cursor 선택 커서
     * @param limit 실제 조회 수
     * @return 조건에 맞는 콘텐츠
     */
    List<Content> findPage(ContentType type, ContentStatus status, ContentCursor cursor, int limit);
}
