package com.commerce.service_catalog.events;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class CatalogEventPublisher {

    private static final String TOPIC = "catalog.events";
    private final KafkaTemplate<String, CatalogEvent> kafkaTemplate;

    public CatalogEventPublisher(KafkaTemplate<String, CatalogEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void productUpserted(String sku, String name, Integer price) {
        CatalogEvent event = new CatalogEvent(
            UUID.randomUUID().toString(),
            "PRODUCT_UPSERTED",
            sku,
            name,
            price,
            Instant.now()
        );
        kafkaTemplate.send(TOPIC, sku, event);
    }
}
