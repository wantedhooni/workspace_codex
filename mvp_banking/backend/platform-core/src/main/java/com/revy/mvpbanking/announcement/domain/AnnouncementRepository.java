package com.revy.mvpbanking.announcement.domain;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnnouncementRepository extends JpaRepository<Announcement, UUID> {
    Optional<Announcement> findByAnnouncementKey(String announcementKey);

    List<Announcement> findAllByOrderByPinnedDescCreatedAtDesc();

    @Query("""
            select announcement
            from Announcement announcement
            where announcement.status = :status
              and announcement.audience in :audiences
              and (announcement.startsAt is null or announcement.startsAt <= :now)
              and (announcement.endsAt is null or announcement.endsAt >= :now)
            order by announcement.pinned desc, announcement.publishedAt desc, announcement.createdAt desc
            """)
    List<Announcement> findVisibleAnnouncements(
            @Param("status") AnnouncementStatus status,
            @Param("audiences") Collection<AnnouncementAudience> audiences,
            @Param("now") Instant now
    );
}
