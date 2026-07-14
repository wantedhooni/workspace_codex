package com.example.commerce.user.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 회원 엔티티의 기본 CRUD와 이메일·가입 순서 조회를 제공하는 Spring Data 저장소다.
 */
public interface SpringDataUserJpaRepository extends JpaRepository<UserEntity, UUID> {

    /**
     * 정규화된 이메일로 회원 엔티티를 조회한다.
     *
     * @param email 정규화된 이메일
     * @return 존재할 경우 회원 엔티티
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * 가입 시각과 식별자 순서로 전체 회원 엔티티를 조회한다.
     *
     * @return 정렬된 회원 엔티티 목록
     */
    List<UserEntity> findAllByOrderByCreatedAtAscIdAsc();
}

