package com.example.samplebatchquartzdashboard.batchsample;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class BatchSampleDatasetService {

    private static final List<String> INSTRUMENTS = List.of("USDKRW-NDF", "EURUSD-FWD", "US10Y-IRS", "KOSPI200-FUT");

    private final JdbcTemplate jdbcTemplate;
    private final BatchSampleProperties properties;

    public BatchSampleDatasetService(JdbcTemplate jdbcTemplate, BatchSampleProperties properties) {
        this.jdbcTemplate = jdbcTemplate;
        this.properties = properties;
    }

    public BatchSampleSeedResponse seed(BatchSampleSeedRequest request) {
        if (request.truncateBeforeLoad()) {
            jdbcTemplate.update("truncate table batch_sample_output restart identity");
            jdbcTemplate.update("truncate table batch_sample_input restart identity");
        }

        int size = request.size() <= 0 ? properties.getDefaultSeedSize() : request.size();
        List<Object[]> rows = new ArrayList<>(size);
        Instant now = Instant.now();
        for (int i = 0; i < size; i++) {
            BigDecimal quantity = new BigDecimal(100_000 + (i % 5_000));
            BigDecimal unitPrice = new BigDecimal("0.5").add(new BigDecimal((i % 1200)).movePointLeft(3));
            rows.add(new Object[]{
                    "ACC-" + String.format("%06d", (i % 50_000) + 1),
                    INSTRUMENTS.get(i % INSTRUMENTS.size()),
                    quantity,
                    unitPrice,
                    Timestamp.from(now.minusSeconds(i % 90))
            });
        }

        jdbcTemplate.batchUpdate(
                "insert into batch_sample_input (account_no, instrument_code, quantity, unit_price, created_at) values (?, ?, ?, ?, ?)",
                rows
        );

        return new BatchSampleSeedResponse(size, pendingCount(), properties.getChunkSize(), properties.getPageSize());
    }

    private long pendingCount() {
        Long count = jdbcTemplate.queryForObject("select count(*) from batch_sample_input where processed = false", Long.class);
        return count == null ? 0L : count;
    }
}
