package com.commerce.service_notification.events;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private final NotificationInbox inbox;

    public NotificationConsumer(NotificationInbox inbox) {
        this.inbox = inbox;
    }

    @KafkaListener(topics = "order.events", groupId = "notification-service")
    public void onOrderEvent(OrderEvent event) {
        if (event == null) return;
        inbox.add(event);
    }
}
