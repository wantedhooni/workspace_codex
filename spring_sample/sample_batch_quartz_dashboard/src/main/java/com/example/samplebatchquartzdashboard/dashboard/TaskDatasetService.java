package com.example.samplebatchquartzdashboard.dashboard;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class TaskDatasetService {

    private static final String INSERT_SQL = """
            insert into task_import_request (
                external_id,
                source_system,
                account_no,
                instrument_code,
                market,
                settlement_currency,
                notional_amount,
                payload_size,
                priority,
                requested_at
            ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final List<String> SOURCE_SYSTEMS = List.of("OMS", "RISK-GW", "SETTLEMENT-HUB", "BOOKING-API");
    private static final List<String> MARKETS = List.of("EXCH", "OTC");
    private static final List<String> INSTRUMENTS = List.of("USDKRW-NDF", "EURUSD-FWD", "US10Y-IRS", "KOSPI200-FUT");
    private static final List<String> CURRENCIES = List.of("USD", "KRW", "EUR");

    private final JdbcTemplate jdbcTemplate;

    public TaskDatasetService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public TaskSeedResponse seed(TaskSeedRequest request) {
        clearIfRequested(request.truncateBeforeLoad());
        List<Object[]> batchArgs = buildSyntheticRows(request.size(), Instant.now());
        jdbcTemplate.batchUpdate(INSERT_SQL, batchArgs);
        return new TaskSeedResponse(request.size(), pendingCount(), "SYNTHETIC");
    }

    public TaskSeedResponse seedRealSample(boolean truncateBeforeLoad) {
        clearIfRequested(truncateBeforeLoad);
        List<Object[]> batchArgs = loadRealSampleRows();
        jdbcTemplate.batchUpdate(INSERT_SQL, batchArgs);
        return new TaskSeedResponse(batchArgs.size(), pendingCount(), "REAL_SAMPLE");
    }

    private void clearIfRequested(boolean truncateBeforeLoad) {
        if (!truncateBeforeLoad) {
            return;
        }
        jdbcTemplate.update("truncate table task_import_audit restart identity");
        jdbcTemplate.update("truncate table task_import_request restart identity");
    }

    private long pendingCount() {
        Long pendingCount = jdbcTemplate.queryForObject(
                "select count(*) from task_import_request where processed = false",
                Long.class
        );
        return pendingCount == null ? 0L : pendingCount;
    }

    private List<Object[]> buildSyntheticRows(int size, Instant now) {
        List<Object[]> rows = new ArrayList<>(size);
        for (int index = 0; index < size; index++) {
            String sourceSystem = SOURCE_SYSTEMS.get(index % SOURCE_SYSTEMS.size());
            String market = MARKETS.get(index % MARKETS.size());
            String instrument = INSTRUMENTS.get(index % INSTRUMENTS.size());
            String currency = CURRENCIES.get(index % CURRENCIES.size());
            int priority = 1 + (index % 5);
            int payloadSize = 120 + (index % 700);
            BigDecimal notionalAmount = new BigDecimal(500_000 + ((long) (index % 2_500) * 10_000L));
            rows.add(new Object[]{
                    "TASK-" + String.format("%08d", index + 1),
                    sourceSystem,
                    "ACC-" + String.format("%06d", (index % 20_000) + 1),
                    instrument,
                    market,
                    currency,
                    notionalAmount,
                    payloadSize,
                    priority,
                    Timestamp.from(now.minusSeconds(index % 180))
            });
        }
        return rows;
    }

    private List<Object[]> loadRealSampleRows() {
        List<Object[]> rows = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource("sample-data/task-import-real-data.csv");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                String[] values = line.split(",");
                rows.add(new Object[]{
                        values[0].trim(),
                        values[1].trim(),
                        values[2].trim(),
                        values[3].trim(),
                        values[4].trim(),
                        values[5].trim(),
                        new BigDecimal(values[6].trim()),
                        Integer.parseInt(values[7].trim()),
                        Integer.parseInt(values[8].trim()),
                        Timestamp.from(Instant.parse(values[9].trim()))
                });
            }
            return rows;
        } catch (Exception exception) {
            throw new IllegalStateException("실제 샘플 데이터 로딩에 실패했습니다.", exception);
        }
    }
}
