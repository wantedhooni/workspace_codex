package com.revy.mvpbanking.announcement.presentation;

import com.revy.mvpbanking.announcement.application.AnnouncementService;
import com.revy.mvpbanking.audit.application.AuditLogService;
import com.revy.mvpbanking.audit.domain.AuditActionType;
import com.revy.mvpbanking.common.api.ApiResponse;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("user-api")
@RequestMapping("/api/user/announcements")
public class UserAnnouncementController {

    private final AnnouncementService announcementService;
    private final AuditLogService auditLogService;

    public UserAnnouncementController(
            AnnouncementService announcementService,
            AuditLogService auditLogService
    ) {
        this.announcementService = announcementService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ApiResponse<List<AnnouncementResponse>> list() {
        auditLogService.logCurrentActor(AuditActionType.USER_ANNOUNCEMENT_VIEWED, "ANNOUNCEMENT", "active", "Viewed active announcements");
        return ApiResponse.ok(announcementService.getActiveUserAnnouncements().stream().map(AnnouncementResponse::from).toList());
    }
}
