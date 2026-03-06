package com.example.samplebatchquartzdashboard.batchsample;

import java.time.Instant;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class BatchSampleJobService {

    private final JobLauncher jobLauncher;
    private final Job batchComponentSampleJob;

    public BatchSampleJobService(
            JobLauncher jobLauncher,
            @Qualifier("batchComponentSampleJob") Job batchComponentSampleJob
    ) {
        this.jobLauncher = jobLauncher;
        this.batchComponentSampleJob = batchComponentSampleJob;
    }

    public JobExecution launchNow() {
        try {
            return jobLauncher.run(batchComponentSampleJob, new JobParametersBuilder()
                    .addLong("requestedAt", Instant.now().toEpochMilli())
                    .addString("trigger", "MANUAL")
                    .toJobParameters());
        } catch (Exception exception) {
            throw new IllegalStateException("Batch 컴포넌트 샘플 잡 실행 실패", exception);
        }
    }
}
