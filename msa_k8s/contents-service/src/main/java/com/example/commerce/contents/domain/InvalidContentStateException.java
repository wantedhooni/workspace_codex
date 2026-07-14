package com.example.commerce.contents.domain;

/**
 * 허용되지 않는 콘텐츠 상태 전이나 수정 요청에 사용하는 예외다.
 */
public class InvalidContentStateException extends RuntimeException {

    /**
     * 현재 상태와 요청 작업을 포함해 예외를 생성한다.
     *
     * @param status 현재 콘텐츠 상태
     * @param operation 요청 작업
     */
    public InvalidContentStateException(ContentStatus status, String operation) {
        super("콘텐츠 상태 %s에서는 %s 작업을 수행할 수 없습니다.".formatted(status, operation));
    }
}
