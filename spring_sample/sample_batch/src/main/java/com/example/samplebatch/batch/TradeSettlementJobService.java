package com.example.samplebatch.batch;

import java.time.Instant;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;

@Service
public class TradeSettlementJobService {

    private final JobLauncher jobLauncher;
    private final Job tradeSettlementJob;

    public TradeSettlementJobService(JobLauncher jobLauncher, Job tradeSettlementJob) {
        this.jobLauncher = jobLauncher;
        this.tradeSettlementJob = tradeSettlementJob;
    }

    public JobExecution launchNow(String trigger) {
        try {
            return jobLauncher.run(tradeSettlementJob, buildParameters(trigger, Instant.now()));
        } catch (Exception exception) {
            throw new IllegalStateException("배치 잡 실행에 실패했습니다.", exception);
        }
    }

    public JobExecution launchFromQuartz(Instant scheduledAt) {
        try {
            return jobLauncher.run(tradeSettlementJob, buildParameters("QUARTZ", scheduledAt));
        } catch (Exception exception) {
            throw new IllegalStateException("Quartz 배치 실행에 실패했습니다.", exception);
        }
    }

    private JobParameters buildParameters(String trigger, Instant requestedAt) {
        return new JobParametersBuilder()
                .addString("trigger", trigger)
                .addLong("requestedAt", requestedAt.toEpochMilli())
                .toJobParameters();
    }
}
