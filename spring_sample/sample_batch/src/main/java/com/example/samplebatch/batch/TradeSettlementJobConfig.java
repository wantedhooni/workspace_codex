package com.example.samplebatch.batch;

import com.example.samplebatch.trade.TradeRawEvent;
import com.example.samplebatch.trade.TradeSettlementItem;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
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
import org.springframework.batch.repeat.exception.ExceptionHandler;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableConfigurationProperties(TradeSettlementProperties.class)
public class TradeSettlementJobConfig {

    @Bean
    public Job tradeSettlementJob(JobRepository jobRepository, Step tradeSettlementStep) {
        return new JobBuilder("tradeSettlementJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(tradeSettlementStep)
                .build();
    }

    @Bean
    public Step tradeSettlementStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            JdbcPagingItemReader<TradeRawEvent> tradeRawEventReader,
            ItemProcessor<TradeRawEvent, TradeSettlementItem> tradeSettlementProcessor,
            JdbcBatchItemWriter<TradeSettlementItem> insertTradeSettlementWriter,
            JdbcBatchItemWriter<TradeSettlementItem> markTradeProcessedWriter,
            TradeSettlementProperties properties
    ) {
        return new org.springframework.batch.core.step.builder.StepBuilder("tradeSettlementStep", jobRepository)
                .<TradeRawEvent, TradeSettlementItem>chunk(properties.getChunkSize(), transactionManager)
                .reader(tradeRawEventReader)
                .processor(tradeSettlementProcessor)
                .writer(new CompositeItemWriterBuilder<TradeSettlementItem>()
                        .delegates(insertTradeSettlementWriter, markTradeProcessedWriter)
                        .build())
                .build();
    }

    @Bean
    public JdbcPagingItemReader<TradeRawEvent> tradeRawEventReader(
            DataSource dataSource,
            TradeSettlementProperties properties
    ) throws Exception {
        PostgresPagingQueryProvider queryProvider = new PostgresPagingQueryProvider();
        queryProvider.setSelectClause(
                "select id, account_no, instrument_code, quantity, price, market, executed_at"
        );
        queryProvider.setFromClause("from trade_raw_event");
        queryProvider.setWhereClause("where processed = false");
        queryProvider.setSortKeys(Map.of("id", Order.ASCENDING));

        return new JdbcPagingItemReaderBuilder<TradeRawEvent>()
                .name("tradeRawEventReader")
                .dataSource(dataSource)
                .queryProvider(queryProvider)
                .pageSize(properties.getPageSize())
                .fetchSize(properties.getPageSize())
                .rowMapper(this::mapTradeRawEvent)
                .saveState(false)
                .build();
    }

    @Bean
    public ItemProcessor<TradeRawEvent, TradeSettlementItem> tradeSettlementProcessor() {
        return item -> {
            BigDecimal grossAmount = item.quantity()
                    .multiply(item.price())
                    .setScale(2, RoundingMode.HALF_UP);
            BigDecimal feeRate = "NASDAQ".equals(item.market())
                    ? new BigDecimal("0.0007")
                    : new BigDecimal("0.0009");
            BigDecimal feeAmount = grossAmount.multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
            BigDecimal netAmount = grossAmount.subtract(feeAmount).setScale(2, RoundingMode.HALF_UP);
            LocalDate settlementDate = item.executedAt().toLocalDate().plusDays(2);

            return new TradeSettlementItem(
                    item.id(),
                    item.accountNo(),
                    item.instrumentCode(),
                    grossAmount,
                    feeAmount,
                    netAmount,
                    settlementDate
            );
        };
    }

    @Bean
    public JdbcBatchItemWriter<TradeSettlementItem> insertTradeSettlementWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<TradeSettlementItem>()
                .dataSource(dataSource)
                .sql(
                        """
                        insert into trade_settlement_summary (
                            source_event_id,
                            account_no,
                            instrument_code,
                            gross_amount,
                            fee_amount,
                            net_amount,
                            settlement_date,
                            created_at
                        ) values (?, ?, ?, ?, ?, ?, ?, ?)
                        """
                )
                .itemPreparedStatementSetter(this::setInsertParameters)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<TradeSettlementItem> markTradeProcessedWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<TradeSettlementItem>()
                .dataSource(dataSource)
                .sql("update trade_raw_event set processed = true, processed_at = current_timestamp where id = ?")
                .itemPreparedStatementSetter(this::setUpdateParameters)
                .assertUpdates(true)
                .build();
    }

    private TradeRawEvent mapTradeRawEvent(ResultSet resultSet, int rowNum) throws SQLException {
        return new TradeRawEvent(
                resultSet.getLong("id"),
                resultSet.getString("account_no"),
                resultSet.getString("instrument_code"),
                resultSet.getBigDecimal("quantity"),
                resultSet.getBigDecimal("price"),
                resultSet.getString("market"),
                resultSet.getTimestamp("executed_at").toLocalDateTime()
        );
    }

    private void setInsertParameters(TradeSettlementItem item, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setLong(1, item.sourceEventId());
        preparedStatement.setString(2, item.accountNo());
        preparedStatement.setString(3, item.instrumentCode());
        preparedStatement.setBigDecimal(4, item.grossAmount());
        preparedStatement.setBigDecimal(5, item.feeAmount());
        preparedStatement.setBigDecimal(6, item.netAmount());
        preparedStatement.setObject(7, item.settlementDate());
        preparedStatement.setTimestamp(8, Timestamp.valueOf(item.settlementDate().atStartOfDay()));
    }

    private void setUpdateParameters(TradeSettlementItem item, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setLong(1, item.sourceEventId());
    }
}
