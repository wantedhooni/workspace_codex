package com.example.commerce.contents.api;

import java.net.URI;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.commerce.contents.application.ContentsService;
import com.example.commerce.contents.domain.ContentStatus;
import com.example.commerce.contents.domain.ContentType;

/**
 * 금융 공지사항과 게시물의 작성, 발행, 보관, 커서 조회 HTTP API를 제공한다.
 */
@Validated
@RestController
@RequestMapping("/api/v1/contents")
public class ContentsController {

    private final ContentsService contentsService;

    /**
     * 콘텐츠 애플리케이션 서비스를 주입받아 컨트롤러를 생성한다.
     *
     * @param contentsService 콘텐츠 애플리케이션 서비스
     */
    public ContentsController(ContentsService contentsService) {
        this.contentsService = contentsService;
    }

    /**
     * 신규 공지사항 또는 게시물을 초안으로 생성한다.
     *
     * @param request 콘텐츠 생성 요청
     * @return 생성 위치와 초안 정보
     */
    @PostMapping
    public ResponseEntity<ContentResponse> createContent(@Valid @RequestBody CreateContentRequest request) {
        ContentResponse response = ContentResponse.from(contentsService.createContent(
                request.type(), request.title(), request.body(), request.authorId()));
        return ResponseEntity.created(URI.create("/api/v1/contents/" + response.id())).body(response);
    }

    /**
     * 식별자로 단일 콘텐츠를 조회한다.
     *
     * @param id 콘텐츠 식별자
     * @return 콘텐츠 정보
     */
    @GetMapping("/{id}")
    public ContentResponse getContent(@PathVariable UUID id) {
        return ContentResponse.from(contentsService.getContent(id));
    }

    /**
     * 요청 버전이 최신일 때 콘텐츠 제목과 본문을 수정한다.
     *
     * @param id 콘텐츠 식별자
     * @param request 콘텐츠 수정 요청
     * @return 수정된 콘텐츠
     */
    @PutMapping("/{id}")
    public ContentResponse updateContent(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateContentRequest request) {
        return ContentResponse.from(contentsService.updateContent(
                id, request.title(), request.body(), request.version()));
    }

    /**
     * 초안 콘텐츠를 공개 상태로 전환한다.
     *
     * @param id 콘텐츠 식별자
     * @param request 상태 변경 요청
     * @return 발행된 콘텐츠
     */
    @PostMapping("/{id}/publish")
    public ContentResponse publishContent(
            @PathVariable UUID id,
            @Valid @RequestBody ContentStateRequest request) {
        return ContentResponse.from(contentsService.publishContent(id, request.version()));
    }

    /**
     * 콘텐츠를 보관 상태로 전환한다.
     *
     * @param id 콘텐츠 식별자
     * @param request 상태 변경 요청
     * @return 보관된 콘텐츠
     */
    @PostMapping("/{id}/archive")
    public ContentResponse archiveContent(
            @PathVariable UUID id,
            @Valid @RequestBody ContentStateRequest request) {
        return ContentResponse.from(contentsService.archiveContent(id, request.version()));
    }

    /**
     * 유형과 상태 필터를 적용해 콘텐츠를 커서 기반으로 조회한다.
     *
     * @param type 선택 콘텐츠 유형
     * @param status 선택 콘텐츠 상태
     * @param cursor 선택 다음 페이지 커서
     * @param size 페이지 크기
     * @return 콘텐츠 페이지
     */
    @GetMapping
    public ContentPageResponse getContents(
            @RequestParam(required = false) ContentType type,
            @RequestParam(required = false) ContentStatus status,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
            @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다.")
            int size) {
        return ContentPageResponse.from(contentsService.getContents(type, status, cursor, size));
    }
}
