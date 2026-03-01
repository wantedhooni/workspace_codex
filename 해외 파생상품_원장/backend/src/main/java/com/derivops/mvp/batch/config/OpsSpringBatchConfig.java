package com.derivops.mvp.batch.config;
import com.derivops.mvp.batch.*;
import com.derivops.mvp.batch.api.*;
import com.derivops.mvp.batch.application.*;
import com.derivops.mvp.batch.dto.*;
import com.derivops.mvp.batch.infrastructure.*;
import com.derivops.mvp.batch.job.*;


import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class OpsSpringBatchConfig {

    private final OpsBatchExecutionService opsBatchExecutionService;

    @Bean
    public Job opsBatchJob(JobRepository jobRepository, Step opsBatchStep) {
        return new JobBuilder("opsBatchJob", jobRepository)
                .start(opsBatchStep)
                .build();
    }

    @Bean
    public Step opsBatchStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("opsBatchStep", jobRepository)
                .tasklet(opsBatchTasklet(null, null), transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public org.springframework.batch.core.step.tasklet.Tasklet opsBatchTasklet(
            @Value("#{jobParameters['batchName']}") String batchName,
            @Value("#{jobParameters['triggerSource']}") String triggerSource
    ) {
        return (contribution, chunkContext) -> {
            String safeBatchName = batchName == null || batchName.isBlank() ? "UNKNOWN_BATCH" : batchName;
            String safeTrigger = triggerSource == null || triggerSource.isBlank() ? "UNKNOWN_TRIGGER" : triggerSource;
            opsBatchExecutionService.execute(safeBatchName, safeTrigger);
            return RepeatStatus.FINISHED;
        };
    }
}
