package com.tradeauto.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradeauto.model.DailyBar;
import com.tradeauto.model.Ticker;
import com.tradeauto.repo.DailyBarRepository;
import com.tradeauto.repo.TickerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
public class MarketDataService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final TickerRepository tickerRepository;
    private final DailyBarRepository dailyBarRepository;
    private final String apiKey;
    private final ZoneId zoneId;

    public MarketDataService(RestTemplate restTemplate,
                             ObjectMapper objectMapper,
                             TickerRepository tickerRepository,
                             DailyBarRepository dailyBarRepository,
                             @Value("${app.polygon.apiKey:}") String apiKey,
                             @Value("${app.scheduling.zone:America/New_York}") String zoneId) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.tickerRepository = tickerRepository;
        this.dailyBarRepository = dailyBarRepository;
        this.apiKey = apiKey;
        this.zoneId = ZoneId.of(zoneId);
    }

    public void refreshAllTickers(int lookbackDays) {
        List<Ticker> tickers = tickerRepository.findAll();
        LocalDate end = LocalDate.now(zoneId);
        LocalDate start = end.minusDays(lookbackDays);
        for (Ticker ticker : tickers) {
            if (ticker.isActive()) {
                refreshTicker(ticker, start, end);
            }
        }
    }

    public void refreshTicker(Ticker ticker, LocalDate start, LocalDate end) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("POLYGON_API_KEY is missing");
        }
        String url = String.format(
                "https://api.polygon.io/v2/aggs/ticker/%s/range/1/day/%s/%s?adjusted=true&sort=asc&apiKey=%s",
                ticker.getSymbol(), start, end, apiKey
        );
        String response = restTemplate.getForObject(url, String.class);
        if (response == null) {
            return;
        }
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode results = root.get("results");
            if (results == null || !results.isArray()) {
                return;
            }
            Map<LocalDate, DailyBar> existing = new HashMap<>();
            for (DailyBar bar : dailyBarRepository.findByTickerAndTradeDateBetween(ticker, start, end)) {
                existing.put(bar.getTradeDate(), bar);
            }
            List<DailyBar> toSave = new ArrayList<>();
            for (JsonNode node : results) {
                long t = node.get("t").asLong();
                LocalDate tradeDate = Instant.ofEpochMilli(t).atZone(zoneId).toLocalDate();
                DailyBar bar = existing.getOrDefault(tradeDate, new DailyBar());
                bar.setTicker(ticker);
                bar.setTradeDate(tradeDate);
                bar.setOpen(node.get("o").asDouble());
                bar.setHigh(node.get("h").asDouble());
                bar.setLow(node.get("l").asDouble());
                bar.setClose(node.get("c").asDouble());
                bar.setVolume(node.get("v").asLong());
                toSave.add(bar);
            }
            dailyBarRepository.saveAll(toSave);
        } catch (Exception ignored) {
        }
    }
}
