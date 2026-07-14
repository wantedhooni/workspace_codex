package com.example.commerce.contents.domain;

/**
 * 클라이언트 버전이 최신 콘텐츠 버전과 다를 때 발생하는 예외다.
 */
public class ContentVersionConflictException extends RuntimeException {

    /**
     * 요청 버전과 현재 버전을 포함해 예외를 생성한다.
     *
     * @param requestedVersion 요청 버전
     * @param currentVersion 현재 버전
     */
    public ContentVersionConflictException(long requestedVersion, long currentVersion) {
        super("콘텐츠가 이미 변경되었습니다. 요청 버전=%d, 현재 버전=%d"
                .formatted(requestedVersion, currentVersion));
    }
}
