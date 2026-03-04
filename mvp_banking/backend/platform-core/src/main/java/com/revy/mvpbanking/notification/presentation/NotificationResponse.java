package com.revy.mvpbanking.notification.presentation;

import com.revy.mvpbanking.notification.domain.Notification;
import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String category,
        String severity,
        String title,
        String message,
        String actionPath,
        String referenceType,
        UUID referenceId,
        boolean read,
        Instant readAt,
        Instant createdAt
) {
    public static NotificationResponse from(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getCategory().name(),
                notification.getSeverity().name(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getActionPath(),
                notification.getReferenceType(),
                notification.getReferenceId(),
                notification.getReadAt() != null,
                notification.getReadAt(),
                notification.getCreatedAt()
        );
    }
}
