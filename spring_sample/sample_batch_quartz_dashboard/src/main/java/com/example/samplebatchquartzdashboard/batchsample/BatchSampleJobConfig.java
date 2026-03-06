package com.example.samplebatchquartzdashboard.batchsample;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
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
@EnableConfigurationProperties(BatchSampleProperties.class)
public class BatchSampleJobConfig {

    public static final String JOB_NAME = "batchComponentSampleJob";

    @Bean
    public Job batchComponentSampleJob(JobRepository jobRepository, Step batchComponentSampleStep) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(batchComponentSampleStep)
                .build();
    }

    @Bean
    public Step batchComponentSampleStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JdbcPagingItemReader<BatchSampleInputRow> batchSampleItemReader,
            ItemProcessor<BatchSampleInputRow, BatchSampleOutputItem> batchSampleItemProcessor,
            JdbcBatchItemWriter<BatchSampleOutputItem> batchSampleInsertWriter,
            JdbcBatchItemWriter<BatchSampleOutputItem> batchSampleProcessedWriter,
            BatchSampleProperties properties
    ) {
        return new org.springframework.batch.core.step.builder.StepBuilder("batchComponentSampleStep", jobRepository)
                .<BatchSampleInputRow, BatchSampleOutputItem>chunk(properties.getChunkSize(), transactionManager)
                .reader(batchSampleItemReader)
                .processor(batchSampleItemProcessor)
                .writer(new CompositeItemWriterBuilder<BatchSampleOutputItem>()
                        .delegates(batchSampleInsertWriter, batchSampleProcessedWriter)
                        .build())
                .build();
    }

    @Bean
    public JdbcPagingItemReader<BatchSampleInputRow> batchSampleItemReader(
            DataSource dataSource,
            BatchSampleProperties properties
    ) throws Exception {
        PostgresPagingQueryProvider queryProvider = new PostgresPagingQueryProvider();
        queryProvider.setSelectClause("select id, account_no, instrument_code, quantity, unit_price, created_at");
        queryProvider.setFromClause("from batch_sample_input");
        queryProvider.setWhereClause("where processed = false");
        queryProvider.setSortKeys(Map.of("id", Order.ASCENDING));

        return new JdbcPagingItemReaderBuilder<BatchSampleInputRow>()
                .name("batchSampleItemReader")
                .dataSource(dataSource)
                .queryProvider(queryProvider)
                .pageSize(properties.getPageSize())
                .fetchSize(properties.getPageSize())
                .rowMapper(this::mapInputRow)
                .saveState(false)
                .build();
    }

    @Bean
    public ItemProcessor<BatchSampleInputRow, BatchSampleOutputItem> batchSampleItemProcessor() {
        return item -> {
            BigDecimal gross = item.quantity().multiply(item.unitPrice()).setScale(2, RoundingMode.HALF_UP);
            BigDecimal feeRate = "USDKRW-NDF".equals(item.instrumentCode()) ? new BigDecimal("0.0012") : new BigDecimal("0.0009");
            BigDecimal fee = gross.multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal net = gross.subtract(fee).setScale(2, RoundingMode.HALF_UP);
            String riskGrade = classifyRisk(gross);
            return new BatchSampleOutputItem(
                    item.id(),
                    item.accountNo(),
                    item.instrumentCode(),
                    gross,
                    fee,
                    net,
                    riskGrade
            );
        };
    }

    @Bean
    public JdbcBatchItemWriter<BatchSampleOutputItem> batchSampleInsertWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<BatchSampleOutputItem>()
                .dataSource(dataSource)
                .sql(
                        """
                        insert into batch_sample_output (
                            input_id,
                            account_no,
                            instrument_code,
                            gross_amount,
                            fee_amount,
                            net_amount,
                            risk_grade,
                            created_at
                        ) values (?, ?, ?, ?, ?, ?, ?, ?)
                        """
                )
                .itemPreparedStatementSetter(this::setInsertParameters)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<BatchSampleOutputItem> batchSampleProcessedWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<BatchSampleOutputItem>()
                .dataSource(dataSource)
                .sql("update batch_sample_input set processed = true, processed_at = current_timestamp where id = ?")
                .itemPreparedStatementSetter((item, statement) -> statement.setLong(1, item.inputId()))
                .assertUpdates(true)
                .build();
    }

    private BatchSampleInputRow mapInputRow(ResultSet rs, int rowNum) throws SQLException {
        return new BatchSampleInputRow(
                rs.getLong("id"),
                rs.getString("account_no"),
                rs.getString("instrument_code"),
                rs.getBigDecimal("quantity"),
                rs.getBigDecimal("unit_price"),
                rs.getTimestamp("created_at").toInstant()
        );
    }

    private void setInsertParameters(BatchSampleOutputItem item, PreparedStatement statement) throws SQLException {
        statement.setLong(1, item.inputId());
        statement.setString(2, item.accountNo());
        statement.setString(3, item.instrumentCode());
        statement.setBigDecimal(4, item.grossAmount());
        statement.setBigDecimal(5, item.feeAmount());
        statement.setBigDecimal(6, item.netAmount());
        statement.setString(7, item.riskGrade());
        statement.setTimestamp(8, Timestamp.from(java.time.Instant.now()));
    }

    private String classifyRisk(BigDecimal grossAmount) {
        if (grossAmount.compareTo(new BigDecimal("5000000")) >= 0) {
            return "HIGH";
        }
        if (grossAmount.compareTo(new BigDecimal("1000000")) >= 0) {
            return "MEDIUM";
        }
        return "LOW";
    }
}
