package com.example.commerce.contents.api;

import java.util.List;

import com.example.commerce.contents.domain.ContentPage;

/**
 * 콘텐츠 커서 목록 API의 페이지 응답을 정의한다.
 *
 * @param items 현재 페이지 콘텐츠
 * @param nextCursor 다음 페이지 커서
 * @param hasNext 다음 페이지 존재 여부
 */
public record ContentPageResponse(
        List<ContentResponse> items,
        String nextCursor,
        boolean hasNext
) {

    /**
     * 콘텐츠 도메인 페이지를 API 페이지 응답으로 변환한다.
     *
     * @param page 변환할 콘텐츠 페이지
     * @return 콘텐츠 페이지 응답
     */
    public static ContentPageResponse from(ContentPage page) {
        return new ContentPageResponse(
                page.items().stream().map(ContentResponse::from).toList(),
                page.nextCursor(),
                page.hasNext());
    }
}
