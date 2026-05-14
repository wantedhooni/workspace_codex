package com.example.websample.domain.post;

import com.example.websample.domain.user.User;
import com.example.websample.domain.user.UserJwtPrincipal;
import com.example.websample.domain.user.UserRepository;
import com.example.websample.global.error.BusinessException;
import com.example.websample.global.error.ErrorCode;
import com.example.websample.global.security.jwt.JwtPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 게시글 생성, 조회, 수정, 삭제 비즈니스 로직을 처리하는 서비스입니다. */
@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    /** 검색 조건과 페이징 조건에 맞는 게시글 목록을 조회합니다. */
    @Transactional(readOnly = true)
    public Page<PostResponse> search(PostSearchCondition condition, Pageable pageable) {
        return postRepository.search(condition, pageable).map(PostResponse::from);
    }

    /** 단건 게시글을 조회합니다. */
    @Transactional(readOnly = true)
    public PostResponse get(Long postId) {
        return PostResponse.from(getPost(postId));
    }

    /** 인증된 사용자를 작성자로 하여 새 게시글을 생성합니다. */
    @Transactional
    public PostResponse create(PostCreateRequest request, JwtPrincipal principal) {
        validateUserPrincipal(principal);
        User author = userRepository.findById(principal.id())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Post post = Post.create(request.title(), request.content(), author);
        return PostResponse.from(postRepository.save(post));
    }

    /** 작성자 본인인 경우 게시글 제목과 내용을 수정합니다. */
    @Transactional
    public PostResponse update(Long postId, PostUpdateRequest request, JwtPrincipal principal) {
        validateUserPrincipal(principal);
        Post post = getPost(postId);
        validateOwner(post, principal.id());
        post.update(request.title(), request.content());
        return PostResponse.from(post);
    }

    /** 작성자 본인인 경우 게시글을 삭제합니다. */
    @Transactional
    public void delete(Long postId, JwtPrincipal principal) {
        validateUserPrincipal(principal);
        Post post = getPost(postId);
        validateOwner(post, principal.id());
        postRepository.delete(post);
    }

    private Post getPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
    }

    private void validateOwner(Post post, Long userId) {
        if (!post.isWrittenBy(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private void validateUserPrincipal(JwtPrincipal principal) {
        if (!UserJwtPrincipal.TYPE.equals(principal.principalType())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }
}
