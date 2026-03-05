package com.revy.mvpbanking.announcement.presentation;

import com.revy.mvpbanking.announcement.application.AnnouncementService;
import com.revy.mvpbanking.audit.application.AuditLogService;
import com.revy.mvpbanking.audit.domain.AuditActionType;
import com.revy.mvpbanking.common.api.ApiResponse;
import com.revy.mvpbanking.common.support.CurrentPrincipalProvider;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("admin-api")
@RequestMapping("/api/admin/announcements")
public class AdminAnnouncementController {

    private final AnnouncementService announcementService;
    private final CurrentPrincipalProvider currentPrincipalProvider;
    private final AuditLogService auditLogService;

    public AdminAnnouncementController(
            AnnouncementService announcementService,
            CurrentPrincipalProvider currentPrincipalProvider,
            AuditLogService auditLogService
    ) {
        this.announcementService = announcementService;
        this.currentPrincipalProvider = currentPrincipalProvider;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ApiResponse<List<AnnouncementResponse>> list() {
        auditLogService.logCurrentActor(AuditActionType.ANNOUNCEMENT_LIST_VIEWED, "ANNOUNCEMENT", "all", "Viewed announcements");
        return ApiResponse.ok(announcementService.getAllAnnouncements().stream().map(AnnouncementResponse::from).toList());
    }

    @PostMapping
    public ApiResponse<AnnouncementResponse> create(@RequestBody CreateAnnouncementRequest request) {
        var principal = currentPrincipalProvider.getCurrentPrincipal();
        var announcement = announcementService.create(
                request.title(),
                request.summary(),
                request.body(),
                request.severity(),
                request.audience(),
                request.pinned(),
                request.startsAt(),
                request.endsAt(),
                principal.getUsername()
        );
        auditLogService.logCurrentActor(
                AuditActionType.ANNOUNCEMENT_CREATED,
                "ANNOUNCEMENT",
                announcement.getId().toString(),
                "Created announcement " + announcement.getTitle()
        );
        return ApiResponse.ok(AnnouncementResponse.from(announcement));
    }

    @PostMapping("/{announcementId}/publish")
    public ApiResponse<AnnouncementResponse> publish(@PathVariable UUID announcementId) {
        var announcement = announcementService.publish(announcementId);
        auditLogService.logCurrentActor(
                AuditActionType.ANNOUNCEMENT_PUBLISHED,
                "ANNOUNCEMENT",
                announcement.getId().toString(),
                "Published announcement " + announcement.getTitle()
        );
        return ApiResponse.ok(AnnouncementResponse.from(announcement));
    }

    @PostMapping("/{announcementId}/archive")
    public ApiResponse<AnnouncementResponse> archive(@PathVariable UUID announcementId) {
        var announcement = announcementService.archive(announcementId);
        auditLogService.logCurrentActor(
                AuditActionType.ANNOUNCEMENT_ARCHIVED,
                "ANNOUNCEMENT",
                announcement.getId().toString(),
                "Archived announcement " + announcement.getTitle()
        );
        return ApiResponse.ok(AnnouncementResponse.from(announcement));
    }
}
