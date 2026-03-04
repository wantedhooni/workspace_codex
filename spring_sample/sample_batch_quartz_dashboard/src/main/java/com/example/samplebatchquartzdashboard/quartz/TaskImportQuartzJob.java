package com.example.samplebatchquartzdashboard.quartz;

import com.example.samplebatchquartzdashboard.batch.TaskImportJobService;
import java.time.Instant;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

@DisallowConcurrentExecution
public class TaskImportQuartzJob extends QuartzJobBean {

    @Autowired
    private TaskImportJobService taskImportJobService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            Instant scheduledAt = context.getScheduledFireTime() == null
                    ? Instant.now()
                    : context.getScheduledFireTime().toInstant();
            taskImportJobService.launchFromQuartz(scheduledAt);
        } catch (Exception exception) {
            throw new JobExecutionException("Quartz 배치 실행 중 오류가 발생했습니다.", exception);
        }
    }
}
