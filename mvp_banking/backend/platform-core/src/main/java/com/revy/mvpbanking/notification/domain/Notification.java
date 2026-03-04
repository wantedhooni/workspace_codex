package com.revy.mvpbanking.notification.domain;

import com.revy.mvpbanking.common.domain.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "notification_key", unique = true, length = 120)
    private String notificationKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_type", nullable = false, length = 20)
    private NotificationRecipientType recipientType;

    @Column(name = "recipient_id", nullable = false)
    private UUID recipientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationSeverity severity;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 255)
    private String message;

    @Column(name = "action_path", nullable = false, length = 120)
    private String actionPath;

    @Column(name = "reference_type", length = 40)
    private String referenceType;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "read_at")
    private Instant readAt;

    protected Notification() {
    }

    public Notification(
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
        this.notificationKey = notificationKey;
        this.recipientType = recipientType;
        this.recipientId = recipientId;
        this.category = category;
        this.severity = severity;
        this.title = title;
        this.message = message;
        this.actionPath = actionPath;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
    }

    public void markRead() {
        if (this.readAt == null) {
            this.readAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public String getNotificationKey() {
        return notificationKey;
    }

    public NotificationRecipientType getRecipientType() {
        return recipientType;
    }

    public UUID getRecipientId() {
        return recipientId;
    }

    public NotificationCategory getCategory() {
        return category;
    }

    public NotificationSeverity getSeverity() {
        return severity;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getActionPath() {
        return actionPath;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public UUID getReferenceId() {
        return referenceId;
    }

    public Instant getReadAt() {
        return readAt;
    }
}
