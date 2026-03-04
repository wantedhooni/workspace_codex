package com.example.samplebatchquartzdashboard.dashboard;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class TaskDatasetService {

    private final JdbcTemplate jdbcTemplate;

    public TaskDatasetService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public TaskSeedResponse seed(TaskSeedRequest request) {
        if (request.truncateBeforeLoad()) {
            jdbcTemplate.update("truncate table task_import_audit restart identity");
            jdbcTemplate.update("truncate table task_import_request restart identity");
        }

        List<Object[]> batchArgs = new ArrayList<>(request.size());
        Instant now = Instant.now();
        for (int index = 0; index < request.size(); index++) {
            batchArgs.add(new Object[]{
                    "TASK-" + UUID.randomUUID(),
                    100 + (index % 500),
                    Timestamp.from(now.minusSeconds(index % 120))
            });
        }

        jdbcTemplate.batchUpdate(
                "insert into task_import_request (external_id, payload_size, requested_at) values (?, ?, ?)",
                batchArgs
        );

        Long pendingCount = jdbcTemplate.queryForObject(
                "select count(*) from task_import_request where processed = false",
                Long.class
        );
        return new TaskSeedResponse(request.size(), pendingCount == null ? 0L : pendingCount);
    }
}
