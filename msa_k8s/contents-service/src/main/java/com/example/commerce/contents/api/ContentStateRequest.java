package com.example.commerce.contents.api;

import jakarta.validation.constraints.PositiveOrZero;

/**
 * 콘텐츠 상태 변경 시 동시성 검증에 사용할 버전을 정의한다.
 *
 * @param version 클라이언트가 조회한 콘텐츠 버전
 */
public record ContentStateRequest(
        @PositiveOrZero(message = "버전은 0 이상이어야 합니다.")
        long version
) {
}
