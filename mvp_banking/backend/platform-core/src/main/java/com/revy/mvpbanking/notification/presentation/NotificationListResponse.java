package com.revy.mvpbanking.notification.presentation;

import com.revy.mvpbanking.common.api.PageResponse;
import java.util.List;

public record NotificationListResponse(
        long unreadCount,
        List<NotificationResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public NotificationListResponse(long unreadCount, List<NotificationResponse> items) {
        this(unreadCount, items, 0, items.size(), items.size(), items.isEmpty() ? 0 : 1);
    }

    public static NotificationListResponse from(long unreadCount, PageResponse<NotificationResponse> pageResponse) {
        return new NotificationListResponse(
                unreadCount,
                pageResponse.items(),
                pageResponse.page(),
                pageResponse.size(),
                pageResponse.totalElements(),
                pageResponse.totalPages()
        );
    }
}
