package com.example.commerce.contents.infrastructure.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import com.example.commerce.contents.domain.Content;
import com.example.commerce.contents.domain.ContentCursor;
import com.example.commerce.contents.domain.ContentRepository;
import com.example.commerce.contents.domain.ContentStatus;
import com.example.commerce.contents.domain.ContentType;

/**
 * PostgreSQL을 금융 콘텐츠 원본 저장소로 사용하는 저장소 구현체다.
 */
@Repository
public class PostgresContentRepository implements ContentRepository {

    private final SpringDataContentJpaRepository repository;

    /**
     * 콘텐츠 Spring Data 저장소를 주입받는다.
     *
     * @param repository 콘텐츠 JPA 저장소
     */
    public PostgresContentRepository(SpringDataContentJpaRepository repository) {
        this.repository = repository;
    }

    /**
     * 콘텐츠를 저장하고 증가된 낙관적 잠금 버전을 반환한다.
     *
     * @param content 저장할 콘텐츠
     * @return 저장된 콘텐츠
     */
    @Override
    public Content save(Content content) {
        return repository.saveAndFlush(ContentEntity.from(content)).toDomain();
    }

    /**
     * 식별자로 콘텐츠를 조회한다.
     *
     * @param id 콘텐츠 식별자
     * @return 존재할 경우 콘텐츠
     */
    @Override
    public Optional<Content> findById(UUID id) {
        return repository.findById(id).map(ContentEntity::toDomain);
    }

    /**
     * 동적 필터와 복합 정렬 커서를 적용해 콘텐츠를 제한 조회한다.
     *
     * @param type 선택 콘텐츠 유형
     * @param status 선택 콘텐츠 상태
     * @param cursor 선택 커서
     * @param limit 실제 조회 수
     * @return 조건에 맞는 콘텐츠
     */
    @Override
    public List<Content> findPage(
            ContentType type,
            ContentStatus status,
            ContentCursor cursor,
            int limit) {
        Specification<ContentEntity> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (type != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), type));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (cursor != null) {
                Predicate olderCreatedAt =
                        criteriaBuilder.lessThan(root.get("createdAt"), cursor.createdAt());
                Predicate sameTimeOlderId = criteriaBuilder.and(
                        criteriaBuilder.equal(root.get("createdAt"), cursor.createdAt()),
                        criteriaBuilder.lessThan(root.get("id"), cursor.id()));
                predicates.add(criteriaBuilder.or(olderCreatedAt, sameTimeOlderId));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };

        Sort sort = Sort.by(
                Sort.Order.desc("createdAt"),
                Sort.Order.desc("id"));
        return repository.findAll(specification, PageRequest.of(0, limit, sort))
                .stream()
                .map(ContentEntity::toDomain)
                .toList();
    }
}
