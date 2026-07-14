package com.example.commerce.contents.application;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.example.commerce.contents.domain.ContentCursor;
import com.example.commerce.contents.domain.InvalidCursorException;

/**
 * 콘텐츠 정렬 키를 URL 안전 Base64 커서로 인코딩하고 복원한다.
 */
@Component
public class ContentCursorCodec {

    /**
     * 커서 코덱을 초기화한다.
     */
    public ContentCursorCodec() {
    }

    /**
     * 생성 시각과 식별자를 외부 커서 문자열로 변환한다.
     *
     * @param cursor 인코딩할 정렬 키
     * @return URL 안전 커서
     */
    public String encode(ContentCursor cursor) {
        String value = cursor.createdAt() + "|" + cursor.id();
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 외부 커서 문자열을 콘텐츠 정렬 키로 복원한다.
     *
     * @param value 디코딩할 커서
     * @return 복원한 정렬 키
     */
    public ContentCursor decode(String value) {
        try {
            String decoded = new String(
                    Base64.getUrlDecoder().decode(value),
                    StandardCharsets.UTF_8);
            String[] parts = decoded.split("\\|", -1);
            if (parts.length != 2) {
                throw new IllegalArgumentException("커서 구성요소 수가 올바르지 않습니다.");
            }
            return new ContentCursor(Instant.parse(parts[0]), UUID.fromString(parts[1]));
        } catch (RuntimeException exception) {
            throw new InvalidCursorException(exception);
        }
    }
}
