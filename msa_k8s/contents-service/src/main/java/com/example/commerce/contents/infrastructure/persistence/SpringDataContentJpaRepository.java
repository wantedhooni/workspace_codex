package com.example.commerce.contents.infrastructure.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * 콘텐츠 엔티티의 기본 저장과 동적 조건 조회를 제공하는 Spring Data 저장소다.
 */
public interface SpringDataContentJpaRepository
        extends JpaRepository<ContentEntity, UUID>, JpaSpecificationExecutor<ContentEntity> {
}
