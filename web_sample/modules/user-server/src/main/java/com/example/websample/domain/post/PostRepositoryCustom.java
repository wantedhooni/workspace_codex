package com.example.websample.domain.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/** 게시글 동적 검색 쿼리를 정의하는 커스텀 저장소 계약입니다. */
public interface PostRepositoryCustom {

    Page<Post> search(PostSearchCondition condition, Pageable pageable);
}
