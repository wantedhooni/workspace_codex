package com.revy.mvpbanking.notification.application;

import com.revy.mvpbanking.admin.domain.AdminUser;
import com.revy.mvpbanking.admin.domain.AdminUserRepository;
import com.revy.mvpbanking.notification.domain.Notification;
import com.revy.mvpbanking.notification.domain.NotificationCategory;
import com.revy.mvpbanking.notification.domain.NotificationRecipientType;
import com.revy.mvpbanking.notification.domain.NotificationRepository;
import com.revy.mvpbanking.notification.domain.NotificationSeverity;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final AdminUserRepository adminUserRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            AdminUserRepository adminUserRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.adminUserRepository = adminUserRepository;
    }

    @Transactional(readOnly = true)
    public List<Notification> getAdminNotifications(UUID adminUserId) {
        return notificationRepository.findByRecipientTypeAndRecipientIdOrderByCreatedAtDesc(NotificationRecipientType.ADMIN, adminUserId);
    }

    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(UUID endUserId) {
        return notificationRepository.findByRecipientTypeAndRecipientIdOrderByCreatedAtDesc(NotificationRecipientType.USER, endUserId);
    }

    @Transactional(readOnly = true)
    public long countUnreadAdminNotifications(UUID adminUserId) {
        return notificationRepository.countByRecipientTypeAndRecipientIdAndReadAtIsNull(NotificationRecipientType.ADMIN, adminUserId);
    }

    @Transactional(readOnly = true)
    public long countUnreadUserNotifications(UUID endUserId) {
        return notificationRepository.countByRecipientTypeAndRecipientIdAndReadAtIsNull(NotificationRecipientType.USER, endUserId);
    }

    @Transactional
    public Notification markAdminNotificationRead(UUID adminUserId, UUID notificationId) {
        return markRead(NotificationRecipientType.ADMIN, adminUserId, notificationId);
    }

    @Transactional
    public Notification markUserNotificationRead(UUID endUserId, UUID notificationId) {
        return markRead(NotificationRecipientType.USER, endUserId, notificationId);
    }

    @Transactional
    public void notifyActiveAdmins(
            String notificationKeyPrefix,
            NotificationCategory category,
            NotificationSeverity severity,
            String title,
            String message,
            String actionPath,
            String referenceType,
            UUID referenceId
    ) {
        List<AdminUser> admins = adminUserRepository.findAll().stream()
                .filter(AdminUser::isActive)
                .toList();
        for (AdminUser admin : admins) {
            createIfAbsent(
                    notificationKeyPrefix + ":" + admin.getId(),
                    NotificationRecipientType.ADMIN,
                    admin.getId(),
                    category,
                    severity,
                    title,
                    message,
                    actionPath,
                    referenceType,
                    referenceId
            );
        }
    }

    @Transactional
    public void notifyUser(
            String notificationKey,
            UUID endUserId,
            NotificationCategory category,
            NotificationSeverity severity,
            String title,
            String message,
            String actionPath,
            String referenceType,
            UUID referenceId
    ) {
        if (endUserId == null) {
            return;
        }
        createIfAbsent(
                notificationKey,
                NotificationRecipientType.USER,
                endUserId,
                category,
                severity,
                title,
                message,
                actionPath,
                referenceType,
                referenceId
        );
    }

    @Transactional
    public Notification createIfAbsent(
            String notificationKey,
            NotificationRecipientType recipientType,
            UUID recipientId,
            NotificationCategory category,
            NotificationSeverity severity,
            String title,
            String message,
            String actionPath,
            String referenceType,
            UUID referenceId
    ) {
        return notificationRepository.findByNotificationKey(notificationKey)
                .orElseGet(() -> notificationRepository.save(
                        new Notification(
                                notificationKey,
                                recipientType,
                                recipientId,
                                category,
                                severity,
                                title,
                                message,
                                actionPath,
                                referenceType,
                                referenceId
                        )
                ));
    }

    private Notification markRead(NotificationRecipientType recipientType, UUID recipientId, UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Notification not found"));
        if (notification.getRecipientType() != recipientType || !notification.getRecipientId().equals(recipientId)) {
            throw new ResponseStatusException(FORBIDDEN, "Notification does not belong to current principal");
        }
        notification.markRead();
        return notification;
    }
}
