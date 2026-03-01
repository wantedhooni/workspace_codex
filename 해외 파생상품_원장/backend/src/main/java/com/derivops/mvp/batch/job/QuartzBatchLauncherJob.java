package com.derivops.mvp.batch.job;
import com.derivops.mvp.batch.*;
import com.derivops.mvp.batch.api.*;
import com.derivops.mvp.batch.application.*;
import com.derivops.mvp.batch.dto.*;
import com.derivops.mvp.batch.infrastructure.*;
import com.derivops.mvp.batch.config.*;


import lombok.RequiredArgsConstructor;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@DisallowConcurrentExecution
public class QuartzBatchLauncherJob extends QuartzJobBean {

    private final JobLauncher jobLauncher;

    @Qualifier("opsBatchJob")
    private final Job opsBatchJob;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getMergedJobDataMap();
        JobKey jobKey = context.getJobDetail().getKey();
        String batchName = dataMap.getString("batchName");
        if (batchName == null || batchName.isBlank()) {
            batchName = jobKey.getName();
        }

        JobParameters params = new JobParametersBuilder()
                .addString("batchName", batchName)
                .addString("triggerSource", "QUARTZ:" + context.getTrigger().getKey().getName())
                .addLong("requestedAt", System.currentTimeMillis())
                .toJobParameters();

        try {
            jobLauncher.run(opsBatchJob, params);
        } catch (Exception ex) {
            throw new JobExecutionException("Failed to launch batch " + batchName, ex, false);
        }
    }
}
