package com.example.websample.domain.post;

import org.springframework.data.jpa.repository.JpaRepository;

/** 게시글 기본 영속성 처리를 담당하는 저장소입니다. */
public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {
}
