package com.example.sampleeventdriven.messaging;

public interface DomainEventPublisher {

    void publish(String topic, String key, String payload);
}
