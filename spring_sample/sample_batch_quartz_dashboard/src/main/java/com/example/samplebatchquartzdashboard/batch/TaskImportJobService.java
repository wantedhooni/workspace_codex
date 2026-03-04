package com.example.samplebatchquartzdashboard.batch;

import java.time.Instant;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Service;

@Service
public class TaskImportJobService {

    private final JobLauncher jobLauncher;
    private final Job taskImportJob;

    public TaskImportJobService(JobLauncher jobLauncher, Job taskImportJob) {
        this.jobLauncher = jobLauncher;
        this.taskImportJob = taskImportJob;
    }

    public JobExecution launchNow(String trigger) {
        try {
            return jobLauncher.run(taskImportJob, new JobParametersBuilder()
                    .addString("trigger", trigger)
                    .addLong("requestedAt", Instant.now().toEpochMilli())
                    .toJobParameters());
        } catch (Exception exception) {
            throw new IllegalStateException("대시보드 배치 실행에 실패했습니다.", exception);
        }
    }

    public JobExecution launchFromQuartz(Instant scheduledAt) {
        try {
            return jobLauncher.run(taskImportJob, new JobParametersBuilder()
                    .addString("trigger", "QUARTZ")
                    .addLong("requestedAt", scheduledAt.toEpochMilli())
                    .toJobParameters());
        } catch (Exception exception) {
            throw new IllegalStateException("Quartz 배치 실행에 실패했습니다.", exception);
        }
    }
}
