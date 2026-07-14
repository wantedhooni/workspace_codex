package com.example.commerce.contents.domain;

import java.util.UUID;

/**
 * 요청한 콘텐츠가 존재하지 않을 때 발생하는 예외다.
 */
public class ContentNotFoundException extends RuntimeException {

    /**
     * 조회에 실패한 콘텐츠 식별자를 포함해 예외를 생성한다.
     *
     * @param contentId 콘텐츠 식별자
     */
    public ContentNotFoundException(UUID contentId) {
        super("콘텐츠를 찾을 수 없습니다: " + contentId);
    }
}
