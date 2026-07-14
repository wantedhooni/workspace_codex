package com.example.commerce.contents.domain;

import java.util.List;

/**
 * 콘텐츠 커서 조회 결과와 다음 페이지 존재 여부를 보관한다.
 *
 * @param items 현재 페이지 콘텐츠
 * @param nextCursor 다음 페이지 시작 커서
 * @param hasNext 다음 페이지 존재 여부
 */
public record ContentPage(List<Content> items, String nextCursor, boolean hasNext) {
}
