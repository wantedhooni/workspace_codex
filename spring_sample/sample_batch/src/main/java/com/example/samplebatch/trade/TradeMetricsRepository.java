package com.example.samplebatch.trade;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TradeMetricsRepository {

    private final JdbcTemplate jdbcTemplate;

    public TradeMetricsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public TradeMetricsResponse fetchMetrics() {
        long rawTotal = count("select count(*) from trade_raw_event");
        long rawPending = count("select count(*) from trade_raw_event where processed = false");
        long rawProcessed = count("select count(*) from trade_raw_event where processed = true");
        long summaryTotal = count("select count(*) from trade_settlement_summary");
        return new TradeMetricsResponse(rawTotal, rawPending, rawProcessed, summaryTotal);
    }

    private long count(String sql) {
        Long value = jdbcTemplate.queryForObject(sql, Long.class);
        return value == null ? 0L : value;
    }
}
