package com.example.marketsignal.batch;

import com.example.marketsignal.report.DailyReportResponse;
import com.example.marketsignal.report.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch 작업과 스텝 구성을 정의한다.
 */
@Configuration
@RequiredArgsConstructor
public class BatchConfiguration {

    private final RealMarketSeedService realMarketSeedService;
    private final ReportService reportService;

    /**
     * 실제 시장 시드 적재와 리포트 재생성을 순차 실행하는 배치 작업을 구성한다.
     */
    @Bean
    public Job marketSeedLoadJob(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager
    ) {
        return new JobBuilder(ManagedBatchJob.MARKET_SEED_LOAD.springBatchJobName(), jobRepository)
                .start(new StepBuilder("marketSeedLoadStep", jobRepository)
                        .tasklet((contribution, chunkContext) -> {
                            RealMarketSeedData seedData = realMarketSeedService.syncSeedData();
                            contribution.setExitStatus(new ExitStatus("COMPLETED", "snapshotDate=" + seedData.snapshotDate()));
                            return RepeatStatus.FINISHED;
                        }, transactionManager)
                        .build())
                .next(new StepBuilder("seedReportRefreshStep", jobRepository)
                        .tasklet((contribution, chunkContext) -> {
                            DailyReportResponse response = reportService.generateTodayReport();
                            contribution.setExitStatus(new ExitStatus("COMPLETED", "reportDate=" + response.reportDate()));
                            return RepeatStatus.FINISHED;
                        }, transactionManager)
                        .build())
                .build();
    }

    /**
     * 오늘의 리포트를 새로 계산하는 배치 작업을 구성한다.
     */
    @Bean
    public Job dailyReportGenerationJob(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager
    ) {
        return new JobBuilder(ManagedBatchJob.DAILY_REPORT_GENERATION.springBatchJobName(), jobRepository)
                .start(new StepBuilder("dailyReportGenerationStep", jobRepository)
                        .tasklet((contribution, chunkContext) -> {
                            DailyReportResponse response = reportService.generateTodayReport();
                            contribution.setExitStatus(new ExitStatus("COMPLETED", "reportDate=" + response.reportDate()));
                            return RepeatStatus.FINISHED;
                        }, transactionManager)
                        .build())
                .build();
    }
}
