package com.example.commerce.contents.application;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.commerce.contents.domain.Content;
import com.example.commerce.contents.domain.ContentCursor;
import com.example.commerce.contents.domain.ContentNotFoundException;
import com.example.commerce.contents.domain.ContentPage;
import com.example.commerce.contents.domain.ContentRepository;
import com.example.commerce.contents.domain.ContentStatus;
import com.example.commerce.contents.domain.ContentType;
import com.example.commerce.contents.domain.ContentVersionConflictException;
import com.example.commerce.contents.domain.InvalidContentStateException;
import com.example.commerce.metrics.BusinessMetric;

/**
 * 금융 공지사항과 게시물의 작성, 수정, 발행, 보관, 조회 유스케이스를 조정한다.
 */
@Service
public class ContentsService {

    private final ContentRepository contentRepository;
    private final ContentCursorCodec cursorCodec;
    private final Clock clock;

    /**
     * 콘텐츠 저장소, 커서 코덱, 시계를 주입받아 서비스를 생성한다.
     *
     * @param contentRepository 콘텐츠 저장소
     * @param cursorCodec 커서 인코딩 도구
     * @param clock 콘텐츠 시각 생성용 시계
     */
    public ContentsService(
            ContentRepository contentRepository,
            ContentCursorCodec cursorCodec,
            Clock clock) {
        this.contentRepository = contentRepository;
        this.cursorCodec = cursorCodec;
        this.clock = clock;
    }

    /**
     * 공지사항 또는 게시물을 초안 상태로 생성한다.
     *
     * @param type 콘텐츠 유형
     * @param title 제목
     * @param body 본문
     * @param authorId 작성 회원 식별자
     * @return 생성된 초안
     */
    @Transactional
    @BusinessMetric("contents.create")
    public Content createContent(ContentType type, String title, String body, UUID authorId) {
        Instant now = Instant.now(clock);
        Content content = new Content(
                UUID.randomUUID(),
                type,
                title.strip(),
                body.strip(),
                authorId,
                ContentStatus.DRAFT,
                0L,
                null,
                now,
                now);
        return contentRepository.save(content);
    }

    /**
     * 식별자로 콘텐츠를 조회하고 존재하지 않으면 도메인 예외를 발생시킨다.
     *
     * @param id 콘텐츠 식별자
     * @return 조회된 콘텐츠
     */
    @Transactional(readOnly = true)
    @BusinessMetric("contents.get")
    public Content getContent(UUID id) {
        return contentRepository.findById(id).orElseThrow(() -> new ContentNotFoundException(id));
    }

    /**
     * 보관되지 않은 콘텐츠의 제목과 본문을 요청 버전 기준으로 수정한다.
     *
     * @param id 콘텐츠 식별자
     * @param title 변경 제목
     * @param body 변경 본문
     * @param requestedVersion 클라이언트가 조회한 버전
     * @return 수정된 콘텐츠
     */
    @Transactional
    @BusinessMetric("contents.update")
    public Content updateContent(UUID id, String title, String body, long requestedVersion) {
        Content current = getAndValidateVersion(id, requestedVersion);
        if (current.status() == ContentStatus.ARCHIVED) {
            throw new InvalidContentStateException(current.status(), "수정");
        }
        Content updated = new Content(
                current.id(),
                current.type(),
                title.strip(),
                body.strip(),
                current.authorId(),
                current.status(),
                current.version(),
                current.publishedAt(),
                current.createdAt(),
                Instant.now(clock));
        return saveWithVersionMapping(updated);
    }

    /**
     * 초안 콘텐츠를 공개 상태로 전환하고 최초 발행 시각을 기록한다.
     *
     * @param id 콘텐츠 식별자
     * @param requestedVersion 클라이언트가 조회한 버전
     * @return 발행된 콘텐츠
     */
    @Transactional
    @BusinessMetric("contents.publish")
    public Content publishContent(UUID id, long requestedVersion) {
        Content current = getAndValidateVersion(id, requestedVersion);
        if (current.status() != ContentStatus.DRAFT) {
            throw new InvalidContentStateException(current.status(), "발행");
        }
        Instant now = Instant.now(clock);
        Content published = new Content(
                current.id(),
                current.type(),
                current.title(),
                current.body(),
                current.authorId(),
                ContentStatus.PUBLISHED,
                current.version(),
                now,
                current.createdAt(),
                now);
        return saveWithVersionMapping(published);
    }

    /**
     * 초안 또는 발행 콘텐츠를 보관 상태로 전환한다.
     *
     * @param id 콘텐츠 식별자
     * @param requestedVersion 클라이언트가 조회한 버전
     * @return 보관된 콘텐츠
     */
    @Transactional
    @BusinessMetric("contents.archive")
    public Content archiveContent(UUID id, long requestedVersion) {
        Content current = getAndValidateVersion(id, requestedVersion);
        if (current.status() == ContentStatus.ARCHIVED) {
            throw new InvalidContentStateException(current.status(), "보관");
        }
        Content archived = new Content(
                current.id(),
                current.type(),
                current.title(),
                current.body(),
                current.authorId(),
                ContentStatus.ARCHIVED,
                current.version(),
                current.publishedAt(),
                current.createdAt(),
                Instant.now(clock));
        return saveWithVersionMapping(archived);
    }

    /**
     * 유형과 상태 필터를 적용해 커서 이후 콘텐츠를 생성 역순으로 조회한다.
     *
     * @param type 선택 콘텐츠 유형
     * @param status 선택 콘텐츠 상태
     * @param cursorValue 선택 외부 커서
     * @param size 페이지 크기
     * @return 콘텐츠 페이지와 다음 커서
     */
    @Transactional(readOnly = true)
    @BusinessMetric("contents.list")
    public ContentPage getContents(
            ContentType type,
            ContentStatus status,
            String cursorValue,
            int size) {
        ContentCursor cursor = cursorValue == null || cursorValue.isBlank()
                ? null
                : cursorCodec.decode(cursorValue);
        List<Content> fetched = contentRepository.findPage(type, status, cursor, size + 1);
        boolean hasNext = fetched.size() > size;
        List<Content> items = hasNext ? List.copyOf(fetched.subList(0, size)) : List.copyOf(fetched);
        String nextCursor = hasNext
                ? cursorCodec.encode(new ContentCursor(
                        items.getLast().createdAt(),
                        items.getLast().id()))
                : null;
        return new ContentPage(items, nextCursor, hasNext);
    }

    private Content getAndValidateVersion(UUID id, long requestedVersion) {
        Content current = getContent(id);
        if (current.version() != requestedVersion) {
            throw new ContentVersionConflictException(requestedVersion, current.version());
        }
        return current;
    }

    private Content saveWithVersionMapping(Content content) {
        try {
            return contentRepository.save(content);
        } catch (OptimisticLockingFailureException exception) {
            throw new ContentVersionConflictException(content.version(), content.version() + 1);
        }
    }
}
