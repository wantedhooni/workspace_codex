package com.revy.mvpbanking.announcement.application;

import com.revy.mvpbanking.announcement.domain.Announcement;
import com.revy.mvpbanking.announcement.domain.AnnouncementAudience;
import com.revy.mvpbanking.announcement.domain.AnnouncementRepository;
import com.revy.mvpbanking.announcement.domain.AnnouncementSeverity;
import com.revy.mvpbanking.announcement.domain.AnnouncementStatus;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class AnnouncementService {

    private final AnnouncementRepository announcementRepository;

    public AnnouncementService(AnnouncementRepository announcementRepository) {
        this.announcementRepository = announcementRepository;
    }

    @Transactional(readOnly = true)
    public List<Announcement> getAllAnnouncements() {
        return announcementRepository.findAllByOrderByPinnedDescCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Announcement> getActiveUserAnnouncements() {
        return announcementRepository.findVisibleAnnouncements(
                AnnouncementStatus.PUBLISHED,
                Set.of(AnnouncementAudience.ALL, AnnouncementAudience.USER),
                Instant.now()
        );
    }

    @Transactional
    public Announcement create(
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
        validateSchedule(startsAt, endsAt);
        return announcementRepository.save(
                new Announcement(
                        "ANNOUNCEMENT-" + UUID.randomUUID(),
                        title,
                        summary,
                        body,
                        severity,
                        audience,
                        pinned,
                        startsAt,
                        endsAt,
                        createdByEmail
                )
        );
    }

    @Transactional
    public Announcement publish(UUID announcementId) {
        Announcement announcement = getById(announcementId);
        validateSchedule(announcement.getStartsAt(), announcement.getEndsAt());
        announcement.publish(Instant.now());
        return announcement;
    }

    @Transactional
    public Announcement archive(UUID announcementId) {
        Announcement announcement = getById(announcementId);
        announcement.archive(Instant.now());
        return announcement;
    }

    private Announcement getById(UUID announcementId) {
        return announcementRepository.findById(announcementId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Announcement not found"));
    }

    private void validateSchedule(Instant startsAt, Instant endsAt) {
        if (startsAt != null && endsAt != null && endsAt.isBefore(startsAt)) {
            throw new ResponseStatusException(BAD_REQUEST, "Announcement end time must be after start time");
        }
    }
}
