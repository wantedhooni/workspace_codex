package com.revy.mvpbanking.notification.presentation;

import java.util.List;

public record NotificationListResponse(
        long unreadCount,
        List<NotificationResponse> items
) {
}
