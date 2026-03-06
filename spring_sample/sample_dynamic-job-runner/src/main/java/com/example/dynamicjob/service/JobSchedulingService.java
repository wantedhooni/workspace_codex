package com.example.dynamicjob.service;

import com.example.dynamicjob.entity.JobDefinition;
import com.example.dynamicjob.job.DynamicBeanMethodJob;
import jakarta.annotation.PostConstruct;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Service
public class JobSchedulingService {

    private final Scheduler scheduler;
    private final com.example.dynamicjob.repository.JobDefinitionRepository jobDefinitionRepository;
    private final JobExecutionService jobExecutionService;

    public JobSchedulingService(Scheduler scheduler,
                                com.example.dynamicjob.repository.JobDefinitionRepository jobDefinitionRepository,
                                JobExecutionService jobExecutionService) {
        this.scheduler = scheduler;
        this.jobDefinitionRepository = jobDefinitionRepository;
        this.jobExecutionService = jobExecutionService;
    }

    @PostConstruct
    public void init() throws SchedulerException {
        scheduler.getContext().put("jobDefinitionRepository", jobDefinitionRepository);
        scheduler.getContext().put("jobExecutionService", jobExecutionService);
    }

    public void scheduleAllEnabled(List<JobDefinition> definitions) {
        definitions.forEach(this::scheduleOrReplace);
    }

    public void scheduleOrReplace(JobDefinition def) {
        try {
            JobKey jobKey = JobKey.jobKey(def.getJobName());
            TriggerKey triggerKey = TriggerKey.triggerKey(def.getJobName() + "Trigger");

            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }

            if (!def.isEnabled()) {
                return;
            }

            JobDetail jobDetail = newJob(DynamicBeanMethodJob.class)
                    .withIdentity(jobKey)
                    .usingJobData("jobId", def.getId())
                    .build();

            Trigger trigger = newTrigger()
                    .withIdentity(triggerKey)
                    .forJob(jobDetail)
                    .withSchedule(cronSchedule(def.getCronExpr()))
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to schedule job: " + def.getJobName(), e);
        }
    }

    public void unschedule(String jobName) {
        try {
            scheduler.deleteJob(JobKey.jobKey(jobName));
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to unschedule job: " + jobName, e);
        }
    }

    public void triggerNow(Long jobId) {
        try {
            JobDefinition def = jobDefinitionRepository.findById(jobId)
                    .orElseThrow(() -> new IllegalArgumentException("Job definition not found: " + jobId));
            if (scheduler.checkExists(JobKey.jobKey(def.getJobName()))) {
                scheduler.triggerJob(JobKey.jobKey(def.getJobName()));
            } else {
                jobExecutionService.execute(def);
            }
        } catch (SchedulerException e) {
            throw new IllegalStateException("Failed to trigger job: " + jobId, e);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to execute job: " + jobId, e);
        }
    }
}
