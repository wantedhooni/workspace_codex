package com.commerce.service_notification.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<NotificationMessage, Long> {
    List<NotificationMessage> findAllByOrderByCreatedAtDesc();
    List<NotificationMessage> findAllByReadFlagFalseOrderByCreatedAtDesc();
}
