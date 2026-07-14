package com.example.commerce.contents.domain;

/**
 * 콘텐츠 목록의 커서 형식을 해석할 수 없을 때 발생하는 예외다.
 */
public class InvalidCursorException extends RuntimeException {

    /**
     * 커서 파싱 원인을 보존해 예외를 생성한다.
     *
     * @param cause 원본 파싱 예외
     */
    public InvalidCursorException(Throwable cause) {
        super("콘텐츠 조회 커서 형식이 올바르지 않습니다.", cause);
    }
}
