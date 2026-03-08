package com.revy.mvpbanking.notification.presentation;

import com.revy.mvpbanking.common.api.ApiResponse;
import com.revy.mvpbanking.common.api.PageResponse;
import com.revy.mvpbanking.common.support.CurrentPrincipalProvider;
import com.revy.mvpbanking.notification.application.NotificationService;
import com.revy.mvpbanking.notification.presentation.NotificationListResponse;
import com.revy.mvpbanking.notification.presentation.NotificationResponse;
import java.util.UUID;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("user-api")
@RequestMapping("/api/user/notifications")
public class UserNotificationController {

    private final NotificationService notificationService;
    private final CurrentPrincipalProvider currentPrincipalProvider;

    public UserNotificationController(
            NotificationService notificationService,
            CurrentPrincipalProvider currentPrincipalProvider
    ) {
        this.notificationService = notificationService;
        this.currentPrincipalProvider = currentPrincipalProvider;
    }

    @GetMapping
    public ApiResponse<NotificationListResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var principal = currentPrincipalProvider.getCurrentPrincipal();
        return ApiResponse.ok(NotificationListResponse.from(
                notificationService.countUnreadUserNotifications(principal.getPrincipalId()),
                PageResponse.from(
                        notificationService.getUserNotifications(principal.getPrincipalId()).stream()
                                .map(NotificationResponse::from)
                                .toList(),
                        page,
                        size
                )
        ));
    }

    @PostMapping("/{notificationId}/read")
    public ApiResponse<NotificationResponse> markRead(@PathVariable UUID notificationId) {
        var principal = currentPrincipalProvider.getCurrentPrincipal();
        return ApiResponse.ok(NotificationResponse.from(
                notificationService.markUserNotificationRead(principal.getPrincipalId(), notificationId)
        ));
    }
}
