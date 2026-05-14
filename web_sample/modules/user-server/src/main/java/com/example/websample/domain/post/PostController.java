package com.example.websample.domain.post;

import com.example.websample.global.common.ApiResponse;
import com.example.websample.global.security.jwt.JwtPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 게시글 CRUD API를 제공하는 컨트롤러입니다. */
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ApiResponse<Page<PostResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long authorId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ApiResponse.ok(postService.search(new PostSearchCondition(keyword, authorId), pageable));
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> get(@PathVariable Long postId) {
        return ApiResponse.ok(postService.get(postId));
    }

    @PostMapping
    public ApiResponse<PostResponse> create(
            @Valid @RequestBody PostCreateRequest request,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        return ApiResponse.ok(postService.create(request, principal));
    }

    @PutMapping("/{postId}")
    public ApiResponse<PostResponse> update(
            @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest request,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        return ApiResponse.ok(postService.update(postId, request, principal));
    }

    @DeleteMapping("/{postId}")
    public ApiResponse<Void> delete(
            @PathVariable Long postId,
            @AuthenticationPrincipal JwtPrincipal principal
    ) {
        postService.delete(postId, principal);
        return ApiResponse.ok();
    }
}
