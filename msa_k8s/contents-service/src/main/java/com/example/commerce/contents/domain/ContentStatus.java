package com.example.commerce.contents.domain;

/**
 * 콘텐츠의 작성과 공개 수명주기 상태를 나타낸다.
 */
public enum ContentStatus {
    /** 작성 중이며 아직 공개되지 않은 상태다. */
    DRAFT,
    /** 사용자에게 공개된 상태다. */
    PUBLISHED,
    /** 공개 또는 편집 대상에서 제외해 보관한 상태다. */
    ARCHIVED
}
