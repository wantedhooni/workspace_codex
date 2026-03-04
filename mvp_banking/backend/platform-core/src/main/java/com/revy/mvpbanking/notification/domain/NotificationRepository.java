package com.revy.mvpbanking.notification.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    List<Notification> findByRecipientTypeAndRecipientIdOrderByCreatedAtDesc(NotificationRecipientType recipientType, UUID recipientId);
    long countByRecipientTypeAndRecipientIdAndReadAtIsNull(NotificationRecipientType recipientType, UUID recipientId);
    Optional<Notification> findByNotificationKey(String notificationKey);
}
