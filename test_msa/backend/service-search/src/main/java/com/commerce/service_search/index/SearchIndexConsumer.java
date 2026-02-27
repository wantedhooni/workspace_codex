package com.commerce.service_search.index;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class SearchIndexConsumer {

    private final OpenSearchClient client;

    public SearchIndexConsumer(OpenSearchClient client) {
        this.client = client;
    }

    @KafkaListener(topics = "catalog.events", groupId = "search-indexer")
    public void onCatalogEvent(CatalogEvent event) {
        if (event == null) return;
        client.upsert(event);
    }
}
