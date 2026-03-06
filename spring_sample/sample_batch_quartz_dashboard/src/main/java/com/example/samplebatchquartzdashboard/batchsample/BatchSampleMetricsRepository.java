package com.example.samplebatchquartzdashboard.batchsample;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class BatchSampleMetricsRepository {

    private final JdbcTemplate jdbcTemplate;
    private final BatchSampleProperties properties;

    public BatchSampleMetricsRepository(JdbcTemplate jdbcTemplate, BatchSampleProperties properties) {
        this.jdbcTemplate = jdbcTemplate;
        this.properties = properties;
    }

    public BatchSampleMetricsResponse fetch() {
        long totalInput = queryLong("select count(*) from batch_sample_input");
        long pendingInput = queryLong("select count(*) from batch_sample_input where processed = false");
        long processedInput = queryLong("select count(*) from batch_sample_input where processed = true");
        long outputCount = queryLong("select count(*) from batch_sample_output");
        long batchExecutionCount = queryLong(
                """
                select count(*)
                from batch_job_execution e
                join batch_job_instance i on e.job_instance_id = i.job_instance_id
                where i.job_name = 'batchComponentSampleJob'
                """
        );
        String lastJobStatus = jdbcTemplate.query(
                """
                select e.status
                from batch_job_execution e
                join batch_job_instance i on e.job_instance_id = i.job_instance_id
                where i.job_name = 'batchComponentSampleJob'
                order by e.job_execution_id desc
                limit 1
                """,
                rs -> rs.next() ? rs.getString(1) : "NONE"
        );

        return new BatchSampleMetricsResponse(
                totalInput,
                pendingInput,
                processedInput,
                outputCount,
                batchExecutionCount,
                lastJobStatus,
                properties.getChunkSize(),
                properties.getPageSize()
        );
    }

    private long queryLong(String sql) {
        Long result = jdbcTemplate.queryForObject(sql, Long.class);
        return result == null ? 0L : result;
    }
}
