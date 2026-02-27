package com.commerce.service_search.index;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class OpenSearchClient {

    private final Map<String, CatalogEvent> inMemoryIndex = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${opensearch.url:http://localhost:9200}")
    private String openSearchUrl;

    @Value("${opensearch.index:catalog-items}")
    private String indexName;

    public void upsert(CatalogEvent event) {
        inMemoryIndex.put(event.sku(), event);
        upsertRemote(event);
    }

    public Map<String, CatalogEvent> snapshot() {
        return inMemoryIndex;
    }

    public List<CatalogEvent> search(String query) {
        List<CatalogEvent> remote = searchRemote(query);
        if (!remote.isEmpty()) {
            return remote;
        }
        String q = query == null ? "" : query.toLowerCase();
        return inMemoryIndex.values().stream()
            .filter(item -> item.name().toLowerCase().contains(q) || item.sku().toLowerCase().contains(q))
            .toList();
    }

    private void upsertRemote(CatalogEvent event) {
        try {
            ensureIndex();
            String url = openSearchUrl + "/" + indexName + "/_doc/" + event.sku();
            restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(event), String.class);
        } catch (Exception ignored) {
        }
    }

    private List<CatalogEvent> searchRemote(String query) {
        try {
            ensureIndex();
            String url = openSearchUrl + "/" + indexName + "/_search";
            String body = """
                {
                  "query": {
                    "query_string": {
                      "query": "%s*"
                    }
                  }
                }
                """.formatted(query == null ? "" : query);
            ResponseEntity<String> res = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body), String.class);
            return parseHits(res.getBody());
        } catch (Exception ignored) {
            return List.of();
        }
    }

    private void ensureIndex() {
        try {
            String url = openSearchUrl + "/" + indexName;
            String body = """
                {
                  "settings": {
                    "analysis": {
                      "tokenizer": {
                        "ngram_tokenizer": {
                          "type": "ngram",
                          "min_gram": 2,
                          "max_gram": 10,
                          "token_chars": ["letter", "digit"]
                        }
                      },
                      "analyzer": {
                        "ngram_analyzer": {
                          "type": "custom",
                          "tokenizer": "ngram_tokenizer",
                          "filter": ["lowercase"]
                        }
                      }
                    }
                  },
                  "mappings": {
                    "properties": {
                      "sku": { "type": "keyword" },
                      "name": { "type": "text", "analyzer": "ngram_analyzer" },
                      "price": { "type": "integer" }
                    }
                  }
                }
                """;
            restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(body), String.class);
        } catch (Exception ignored) {
        }
    }

    private List<CatalogEvent> parseHits(String json) throws IOException {
        if (json == null || json.isBlank()) return List.of();
        JsonNode root = mapper.readTree(json);
        JsonNode hits = root.path("hits").path("hits");
        List<CatalogEvent> out = new ArrayList<>();
        for (JsonNode hit : hits) {
            JsonNode source = hit.path("_source");
            CatalogEvent event = mapper.treeToValue(source, CatalogEvent.class);
            out.add(event);
        }
        return out;
    }
}
