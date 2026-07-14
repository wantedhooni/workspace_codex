package com.example.commerce.contents.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import com.example.commerce.contents.domain.Content;
import com.example.commerce.contents.domain.ContentStatus;
import com.example.commerce.contents.domain.ContentType;

/**
 * PostgreSQL의 financial_content 테이블과 콘텐츠 도메인 모델을 연결하는 JPA 엔티티다.
 */
@Entity
@Table(name = "financial_content", schema = "contents")
public class ContentEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String body;

    @Column(name = "author_id", nullable = false, updatable = false)
    private UUID authorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentStatus status;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /**
     * JPA가 엔티티를 복원할 때 사용하는 생성자다.
     */
    protected ContentEntity() {
    }

    private ContentEntity(
            UUID id,
            ContentType type,
            String title,
            String body,
            UUID authorId,
            ContentStatus status,
            long version,
            Instant publishedAt,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.body = body;
        this.authorId = authorId;
        this.status = status;
        this.version = version;
        this.publishedAt = publishedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 콘텐츠 도메인 모델을 영속 엔티티로 변환한다.
     *
     * @param content 변환할 콘텐츠
     * @return 콘텐츠 영속 엔티티
     */
    public static ContentEntity from(Content content) {
        return new ContentEntity(
                content.id(),
                content.type(),
                content.title(),
                content.body(),
                content.authorId(),
                content.status(),
                content.version(),
                content.publishedAt(),
                content.createdAt(),
                content.updatedAt());
    }

    /**
     * 영속 엔티티를 콘텐츠 도메인 모델로 변환한다.
     *
     * @return 콘텐츠 도메인 모델
     */
    public Content toDomain() {
        return new Content(
                id, type, title, body, authorId, status, version, publishedAt, createdAt, updatedAt);
    }
}
