package com.revy.mvpbanking.announcement.domain;

import com.revy.mvpbanking.common.domain.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "announcements")
public class Announcement extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "announcement_key", nullable = false, unique = true, length = 120)
    private String announcementKey;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 255)
    private String summary;

    @Column(nullable = false, columnDefinition = "text")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnnouncementSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnnouncementAudience audience;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AnnouncementStatus status;

    @Column(nullable = false)
    private boolean pinned;

    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "archived_at")
    private Instant archivedAt;

    @Column(name = "created_by_email", nullable = false, length = 120)
    private String createdByEmail;

    protected Announcement() {
    }

    public Announcement(
            String announcementKey,
            String title,
            String summary,
            String body,
            AnnouncementSeverity severity,
            AnnouncementAudience audience,
            boolean pinned,
            Instant startsAt,
            Instant endsAt,
            String createdByEmail
    ) {
        this.announcementKey = announcementKey;
        this.title = title;
        this.summary = summary;
        this.body = body;
        this.severity = severity;
        this.audience = audience;
        this.status = AnnouncementStatus.DRAFT;
        this.pinned = pinned;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.createdByEmail = createdByEmail;
    }

    public void publish(Instant publishedAt) {
        if (this.status == AnnouncementStatus.ARCHIVED) {
            throw new IllegalStateException("Archived announcement cannot be published");
        }
        this.status = AnnouncementStatus.PUBLISHED;
        this.publishedAt = publishedAt;
        this.archivedAt = null;
    }

    public void updateDraft(
            String title,
            String summary,
            String body,
            AnnouncementSeverity severity,
            AnnouncementAudience audience,
            boolean pinned,
            Instant startsAt,
            Instant endsAt
    ) {
        if (this.status != AnnouncementStatus.DRAFT) {
            throw new IllegalStateException("Only draft announcement can be updated");
        }
        this.title = title;
        this.summary = summary;
        this.body = body;
        this.severity = severity;
        this.audience = audience;
        this.pinned = pinned;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    public void archive(Instant archivedAt) {
        this.status = AnnouncementStatus.ARCHIVED;
        this.archivedAt = archivedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getAnnouncementKey() {
        return announcementKey;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getBody() {
        return body;
    }

    public AnnouncementSeverity getSeverity() {
        return severity;
    }

    public AnnouncementAudience getAudience() {
        return audience;
    }

    public AnnouncementStatus getStatus() {
        return status;
    }

    public boolean isPinned() {
        return pinned;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public Instant getArchivedAt() {
        return archivedAt;
    }

    public String getCreatedByEmail() {
        return createdByEmail;
    }
}
