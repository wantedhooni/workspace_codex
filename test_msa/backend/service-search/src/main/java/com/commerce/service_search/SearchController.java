package com.commerce.service_search;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final com.commerce.service_search.index.OpenSearchClient index;

    public SearchController(com.commerce.service_search.index.OpenSearchClient index) {
        this.index = index;
    }

    @GetMapping("/status")
    public String status() {
        return "service-search up";
    }

    @GetMapping("/index")
    public Map<String, com.commerce.service_search.index.CatalogEvent> index() {
        return index.snapshot();
    }

    @GetMapping("/query")
    public List<com.commerce.service_search.index.CatalogEvent> query(@RequestParam("q") String query) {
        return index.search(query);
    }
}
