package com.tradeauto.service;

import com.tradeauto.model.JobRun;
import com.tradeauto.repo.JobRunRepository;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class JobRunService {
    private final JobRunRepository jobRunRepository;

    public JobRunService(JobRunRepository jobRunRepository) {
        this.jobRunRepository = jobRunRepository;
    }

    public JobRun start(String jobName) {
        JobRun run = new JobRun();
        run.setJobName(jobName);
        run.setStatus("RUNNING");
        run.setStartedAt(OffsetDateTime.now());
        return jobRunRepository.save(run);
    }

    public JobRun success(JobRun run, String message) {
        run.setStatus("SUCCESS");
        run.setEndedAt(OffsetDateTime.now());
        run.setMessage(message);
        return jobRunRepository.save(run);
    }

    public JobRun failure(JobRun run, String message) {
        run.setStatus("FAILED");
        run.setEndedAt(OffsetDateTime.now());
        run.setMessage(message);
        return jobRunRepository.save(run);
    }

    public List<JobRun> recent(String jobName) {
        return jobRunRepository.findTop50ByJobNameOrderByStartedAtDesc(jobName);
    }
}
