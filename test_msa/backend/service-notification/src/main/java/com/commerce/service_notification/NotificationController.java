package com.commerce.service_notification;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    private final com.commerce.service_notification.events.NotificationInbox inbox;

    public NotificationController(com.commerce.service_notification.events.NotificationInbox inbox) {
        this.inbox = inbox;
    }

    @GetMapping("/status")
    public String status() {
        return "service-notification up";
    }

    @GetMapping("/inbox")
    public List<com.commerce.service_notification.domain.NotificationMessage> inbox() {
        return inbox.snapshot();
    }

    @GetMapping("/unread")
    public List<com.commerce.service_notification.domain.NotificationMessage> unread() {
        return inbox.unread();
    }

    @PostMapping("/read/{id}")
    public void markRead(@PathVariable Long id) {
        inbox.markRead(id);
    }
}
