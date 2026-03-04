package com.revy.mvpbanking.announcement.presentation;

import com.revy.mvpbanking.announcement.domain.AnnouncementAudience;
import com.revy.mvpbanking.announcement.domain.AnnouncementSeverity;
import java.time.Instant;

public record CreateAnnouncementRequest(
        String title,
        String summary,
        String body,
        AnnouncementSeverity severity,
        AnnouncementAudience audience,
        boolean pinned,
        Instant startsAt,
        Instant endsAt
) {
}
