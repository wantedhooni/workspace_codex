package com.example.dynamicjob.job;

import com.example.dynamicjob.entity.JobDefinition;
import com.example.dynamicjob.repository.JobDefinitionRepository;
import com.example.dynamicjob.service.JobExecutionService;
import org.quartz.*;

@DisallowConcurrentExecution
public class DynamicBeanMethodJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            SchedulerContext schedulerContext = context.getScheduler().getContext();
            JobDefinitionRepository definitionRepository = (JobDefinitionRepository) schedulerContext.get("jobDefinitionRepository");
            JobExecutionService jobExecutionService = (JobExecutionService) schedulerContext.get("jobExecutionService");

            long jobId = context.getMergedJobDataMap().getLong("jobId");
            JobDefinition def = definitionRepository.findById(jobId)
                    .orElseThrow(() -> new IllegalArgumentException("Job definition not found: " + jobId));

            if (!def.isEnabled()) {
                return;
            }

            jobExecutionService.execute(def);
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }
}
