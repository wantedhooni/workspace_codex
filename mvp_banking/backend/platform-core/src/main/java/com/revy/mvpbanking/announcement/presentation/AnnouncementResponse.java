package com.revy.mvpbanking.announcement.presentation;

import com.revy.mvpbanking.announcement.domain.Announcement;
import java.time.Instant;
import java.util.UUID;

public record AnnouncementResponse(
        UUID id,
        String title,
        String summary,
        String body,
        String severity,
        String audience,
        String status,
        boolean pinned,
        Instant startsAt,
        Instant endsAt,
        Instant publishedAt,
        Instant archivedAt,
        String createdByEmail,
        Instant createdAt
) {
    public static AnnouncementResponse from(Announcement announcement) {
        return new AnnouncementResponse(
                announcement.getId(),
                announcement.getTitle(),
                announcement.getSummary(),
                announcement.getBody(),
                announcement.getSeverity().name(),
                announcement.getAudience().name(),
                announcement.getStatus().name(),
                announcement.isPinned(),
                announcement.getStartsAt(),
                announcement.getEndsAt(),
                announcement.getPublishedAt(),
                announcement.getArchivedAt(),
                announcement.getCreatedByEmail(),
                announcement.getCreatedAt()
        );
    }
}
