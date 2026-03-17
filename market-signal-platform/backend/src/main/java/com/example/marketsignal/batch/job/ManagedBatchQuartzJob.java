package com.example.marketsignal.batch;

import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * Quartz 트리거에서 Spring Batch 작업 실행을 위임한다.
 */
@Slf4j
@DisallowConcurrentExecution
public class ManagedBatchQuartzJob extends QuartzJobBean {

    public static final String MANAGED_JOB_NAME_KEY = "managedJobName";

    @Autowired
    private BatchOperationsService batchOperationsService;

    /**
     * Quartz 실행 시점에 연결된 관리 대상 배치 작업을 실행한다.
     */
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getMergedJobDataMap();
        String managedJobName = dataMap.getString(MANAGED_JOB_NAME_KEY);

        try {
            batchOperationsService.launchScheduledJob(managedJobName);
        } catch (Exception exception) {
            log.warn("Quartz 배치 실행에 실패했습니다. job={}", managedJobName, exception);
            throw new JobExecutionException(exception, false);
        }
    }
}
