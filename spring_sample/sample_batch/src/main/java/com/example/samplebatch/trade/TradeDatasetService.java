package com.example.samplebatch.trade;

import com.example.samplebatch.batch.TradeSettlementProperties;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TradeDatasetService {

    private final JdbcTemplate jdbcTemplate;
    private final TradeMetricsRepository tradeMetricsRepository;
    private final TradeSettlementProperties properties;

    public TradeDatasetService(
            JdbcTemplate jdbcTemplate,
            TradeMetricsRepository tradeMetricsRepository,
            TradeSettlementProperties properties
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.tradeMetricsRepository = tradeMetricsRepository;
        this.properties = properties;
    }

    public TradeSeedResponse seed(TradeSeedRequest request) {
        int batchSize = request.batchSize() > 0 ? request.batchSize() : properties.getDefaultSeedBatchSize();
        if (request.truncateBeforeLoad()) {
            truncateTables();
        }

        Instant startedAt = Instant.now();
        int inserted = 0;

        while (inserted < request.size()) {
            int currentBatchSize = Math.min(batchSize, request.size() - inserted);
            List<TradeSeedRow> rows = createRows(inserted, currentBatchSize);

            jdbcTemplate.batchUpdate(
                    """
                    insert into trade_raw_event (
                        account_no,
                        instrument_code,
                        quantity,
                        price,
                        market,
                        executed_at,
                        processed
                    ) values (?, ?, ?, ?, ?, ?, false)
                    """,
                    rows,
                    rows.size(),
                    (PreparedStatement ps, TradeSeedRow row) -> {
                        ps.setString(1, row.accountNo());
                        ps.setString(2, row.instrumentCode());
                        ps.setBigDecimal(3, row.quantity());
                        ps.setBigDecimal(4, row.price());
                        ps.setString(5, row.market());
                        ps.setTimestamp(6, Timestamp.valueOf(row.executedAt()));
                    }
            );

            inserted += currentBatchSize;
        }

        return new TradeSeedResponse(
                request.size(),
                inserted,
                batchSize,
                Duration.between(startedAt, Instant.now()),
                tradeMetricsRepository.fetchMetrics()
        );
    }

    @Transactional
    public void truncateTables() {
        jdbcTemplate.execute("TRUNCATE TABLE trade_settlement_summary RESTART IDENTITY");
        jdbcTemplate.execute("TRUNCATE TABLE trade_raw_event RESTART IDENTITY");
    }

    private List<TradeSeedRow> createRows(int offset, int size) {
        List<TradeSeedRow> rows = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            int sequence = offset + index + 1;
            rows.add(new TradeSeedRow(
                    "ACC" + String.format("%08d", (sequence % 5000) + 1),
                    instrument(sequence),
                    BigDecimal.valueOf((sequence % 200) + 1L).setScale(2, RoundingMode.HALF_UP),
                    BigDecimal.valueOf(10_000L + (sequence % 50_000L), 2).setScale(2, RoundingMode.HALF_UP),
                    market(sequence),
                    LocalDateTime.now().minusMinutes(sequence % 10_000L)
            ));
        }
        return rows;
    }

    private String instrument(int sequence) {
        return switch (sequence % 5) {
            case 0 -> "AAPL";
            case 1 -> "NVDA";
            case 2 -> "TSLA";
            case 3 -> "MSFT";
            default -> "AMZN";
        };
    }

    private String market(int sequence) {
        return sequence % 2 == 0 ? "NASDAQ" : "NYSE";
    }

    private record TradeSeedRow(
            String accountNo,
            String instrumentCode,
            BigDecimal quantity,
            BigDecimal price,
            String market,
            LocalDateTime executedAt
    ) {
    }
}
