package com.tradeauto.service;

import com.tradeauto.model.Ticker;
import com.tradeauto.repo.TickerRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
public class SeedService {
    private final TickerRepository tickerRepository;

    public SeedService(TickerRepository tickerRepository) {
        this.tickerRepository = tickerRepository;
    }

    public int seedTickers() {
        int created = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new ClassPathResource("tickers.csv").getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("symbol")) {
                    continue;
                }
                String[] parts = line.split(",", -1);
                if (parts.length < 3) {
                    continue;
                }
                String symbol = parts[0].trim();
                if (symbol.isBlank()) {
                    continue;
                }
                if (tickerRepository.findBySymbol(symbol).isPresent()) {
                    continue;
                }
                Ticker t = new Ticker();
                t.setSymbol(symbol);
                t.setName(parts[1].trim());
                t.setSector(parts[2].trim());
                t.setActive(true);
                tickerRepository.save(t);
                created++;
            }
        } catch (Exception ignored) {
        }
        return created;
    }
}
