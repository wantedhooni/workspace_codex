package com.commerce.service_notification.events;

import com.commerce.service_notification.domain.NotificationMessage;
import com.commerce.service_notification.domain.NotificationRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class NotificationInbox {

    private final NotificationRepository repository;

    public NotificationInbox(NotificationRepository repository) {
        this.repository = repository;
    }

    public void add(OrderEvent event) {
        NotificationMessage message = new NotificationMessage(
            event.type(),
            event.orderId(),
            event.customerId(),
            event.totalAmount(),
            event.status(),
            event.occurredAt() == null ? Instant.now() : event.occurredAt()
        );
        repository.save(message);
    }

    public List<NotificationMessage> snapshot() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public List<NotificationMessage> unread() {
        return repository.findAllByReadFlagFalseOrderByCreatedAtDesc();
    }

    public void markRead(Long id) {
        repository.findById(id).ifPresent(message -> {
            message.markRead();
            repository.save(message);
        });
    }
}
