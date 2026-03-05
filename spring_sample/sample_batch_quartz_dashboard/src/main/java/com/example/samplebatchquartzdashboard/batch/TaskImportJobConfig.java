package com.example.samplebatchquartzdashboard.batch;

import com.example.samplebatchquartzdashboard.dashboard.TaskImportAuditItem;
import com.example.samplebatchquartzdashboard.dashboard.TaskImportRequestRow;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.PostgresPagingQueryProvider;
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableConfigurationProperties(BatchDashboardProperties.class)
public class TaskImportJobConfig {

    @Bean
    public Job taskImportJob(JobRepository jobRepository, Step taskImportStep) {
        return new JobBuilder("taskImportJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(taskImportStep)
                .build();
    }

    @Bean
    public Step taskImportStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JdbcPagingItemReader<TaskImportRequestRow> taskImportRequestReader,
            ItemProcessor<TaskImportRequestRow, TaskImportAuditItem> taskImportProcessor,
            JdbcBatchItemWriter<TaskImportAuditItem> insertTaskImportAuditWriter,
            JdbcBatchItemWriter<TaskImportAuditItem> markTaskProcessedWriter,
            BatchDashboardProperties properties
    ) {
        return new org.springframework.batch.core.step.builder.StepBuilder("taskImportStep", jobRepository)
                .<TaskImportRequestRow, TaskImportAuditItem>chunk(properties.getChunkSize(), transactionManager)
                .reader(taskImportRequestReader)
                .processor(taskImportProcessor)
                .writer(new CompositeItemWriterBuilder<TaskImportAuditItem>()
                        .delegates(insertTaskImportAuditWriter, markTaskProcessedWriter)
                        .build())
                .build();
    }

    @Bean
    public JdbcPagingItemReader<TaskImportRequestRow> taskImportRequestReader(
            DataSource dataSource,
            BatchDashboardProperties properties
    ) throws Exception {
        PostgresPagingQueryProvider queryProvider = new PostgresPagingQueryProvider();
        queryProvider.setSelectClause(
                "select id, external_id, source_system, account_no, instrument_code, market, settlement_currency, "
                        + "notional_amount, payload_size, priority, requested_at"
        );
        queryProvider.setFromClause("from task_import_request");
        queryProvider.setWhereClause("where processed = false");
        queryProvider.setSortKeys(Map.of("id", Order.ASCENDING));

        return new JdbcPagingItemReaderBuilder<TaskImportRequestRow>()
                .name("taskImportRequestReader")
                .dataSource(dataSource)
                .queryProvider(queryProvider)
                .pageSize(properties.getPageSize())
                .fetchSize(properties.getPageSize())
                .rowMapper(this::mapRequestRow)
                .saveState(false)
                .build();
    }

    @Bean
    public ItemProcessor<TaskImportRequestRow, TaskImportAuditItem> taskImportProcessor() {
        return item -> {
            long marketFactor = "OTC".equals(item.market()) ? 3L : 2L;
            long priorityFactor = Math.max(1, item.priority());
            long latency = Math.max(60L, (item.payloadSize() * marketFactor) + (priorityFactor * 15L));
            String riskBucket = classifyRisk(item.notionalAmount(), item.market());

            return new TaskImportAuditItem(
                    item.id(),
                    item.externalId(),
                    item.sourceSystem(),
                    item.accountNo(),
                    item.instrumentCode(),
                    item.market(),
                    item.settlementCurrency(),
                    item.notionalAmount(),
                    item.priority(),
                    riskBucket,
                    item.payloadSize(),
                    latency,
                    Instant.now()
            );
        };
    }

    @Bean
    public JdbcBatchItemWriter<TaskImportAuditItem> insertTaskImportAuditWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<TaskImportAuditItem>()
                .dataSource(dataSource)
                .sql(
                        """
                        insert into task_import_audit (
                            request_id,
                            external_id,
                            source_system,
                            account_no,
                            instrument_code,
                            market,
                            settlement_currency,
                            notional_amount,
                            priority,
                            risk_bucket,
                            payload_size,
                            processing_latency_ms,
                            created_at
                        ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """
                )
                .itemPreparedStatementSetter(this::setInsertParameters)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<TaskImportAuditItem> markTaskProcessedWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<TaskImportAuditItem>()
                .dataSource(dataSource)
                .sql("update task_import_request set processed = true, processed_at = current_timestamp where id = ?")
                .itemPreparedStatementSetter(this::setUpdateParameters)
                .assertUpdates(true)
                .build();
    }

    private TaskImportRequestRow mapRequestRow(ResultSet resultSet, int rowNum) throws SQLException {
        return new TaskImportRequestRow(
                resultSet.getLong("id"),
                resultSet.getString("external_id"),
                resultSet.getString("source_system"),
                resultSet.getString("account_no"),
                resultSet.getString("instrument_code"),
                resultSet.getString("market"),
                resultSet.getString("settlement_currency"),
                resultSet.getBigDecimal("notional_amount"),
                resultSet.getInt("payload_size"),
                resultSet.getInt("priority"),
                resultSet.getTimestamp("requested_at").toInstant()
        );
    }

    private void setInsertParameters(TaskImportAuditItem item, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setLong(1, item.requestId());
        preparedStatement.setString(2, item.externalId());
        preparedStatement.setString(3, item.sourceSystem());
        preparedStatement.setString(4, item.accountNo());
        preparedStatement.setString(5, item.instrumentCode());
        preparedStatement.setString(6, item.market());
        preparedStatement.setString(7, item.settlementCurrency());
        preparedStatement.setBigDecimal(8, item.notionalAmount());
        preparedStatement.setInt(9, item.priority());
        preparedStatement.setString(10, item.riskBucket());
        preparedStatement.setInt(11, item.payloadSize());
        preparedStatement.setLong(12, item.processingLatencyMs());
        preparedStatement.setTimestamp(13, Timestamp.from(item.createdAt()));
    }

    private void setUpdateParameters(TaskImportAuditItem item, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setLong(1, item.requestId());
    }

    private String classifyRisk(java.math.BigDecimal notionalAmount, String market) {
        if (notionalAmount.compareTo(new java.math.BigDecimal("10000000")) >= 0 || "OTC".equals(market)) {
            return "HIGH";
        }
        if (notionalAmount.compareTo(new java.math.BigDecimal("3000000")) >= 0) {
            return "MEDIUM";
        }
        return "LOW";
    }
}
