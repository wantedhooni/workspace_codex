package com.quant.portal.api.application.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.stereotype.Component;

@Component
public class StooqEodPriceClient {

    private static final DateTimeFormatter STOOQ_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final URI BASE_URI = URI.create("https://stooq.com/q/l/");

    private final HttpClient httpClient;

    public StooqEodPriceClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    public Optional<StooqEodQuote> fetchLatestQuote(String ticker) {
        if (ticker == null || ticker.isBlank()) {
            return Optional.empty();
        }

        String symbol = ticker.trim().toLowerCase(Locale.ROOT) + ".us";
        URI uri = URI.create(BASE_URI + "?s=" + symbol + "&i=d");

        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(2))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return Optional.empty();
            }
            return parseQuote(response.body());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (IOException exception) {
            return Optional.empty();
        }
    }

    private Optional<StooqEodQuote> parseQuote(String csv) {
        if (csv == null || csv.isBlank()) {
            return Optional.empty();
        }

        String firstLine = csv.lines()
                .findFirst()
                .orElse("");
        String[] parts = firstLine.split(",", -1);
        if (parts.length < 7) {
            return Optional.empty();
        }

        String rawDate = parts[1];
        String rawClose = parts[6];

        if (rawDate.isBlank() || rawClose.isBlank() || "N/D".equals(rawDate) || "N/D".equals(rawClose)) {
            return Optional.empty();
        }

        try {
            LocalDate date = LocalDate.parse(rawDate, STOOQ_DATE_FORMATTER);
            BigDecimal closePrice = new BigDecimal(rawClose);
            return Optional.of(new StooqEodQuote(date, closePrice));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    public record StooqEodQuote(
            LocalDate date,
            BigDecimal closePrice
    ) {
    }
}
