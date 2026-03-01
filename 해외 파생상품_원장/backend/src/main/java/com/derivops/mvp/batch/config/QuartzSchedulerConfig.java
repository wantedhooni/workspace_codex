package com.derivops.mvp.batch.config;
import com.derivops.mvp.batch.*;
import com.derivops.mvp.batch.api.*;
import com.derivops.mvp.batch.application.*;
import com.derivops.mvp.batch.dto.*;
import com.derivops.mvp.batch.infrastructure.*;
import com.derivops.mvp.batch.job.*;


import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzSchedulerConfig {

    public static final String GROUP = "DERIVOPS_BATCH";

    @Bean
    public JobDetail positionSyncJobDetail() {
        return buildJobDetail("positionSyncJob", "POSITION_SYNC");
    }

    @Bean
    public Trigger positionSyncTrigger(
            JobDetail positionSyncJobDetail,
            @Value("${derivops.quartz.position-sync-cron:0 0/2 * * * ?}") String cron
    ) {
        return buildTrigger("positionSyncTrigger", positionSyncJobDetail, cron);
    }

    @Bean
    public JobDetail marginRecalcJobDetail() {
        return buildJobDetail("marginRecalcJob", "MARGIN_RECALC");
    }

    @Bean
    public Trigger marginRecalcTrigger(
            JobDetail marginRecalcJobDetail,
            @Value("${derivops.quartz.margin-recalc-cron:0 1/3 * * * ?}") String cron
    ) {
        return buildTrigger("marginRecalcTrigger", marginRecalcJobDetail, cron);
    }

    @Bean
    public JobDetail eodSettlementJobDetail() {
        return buildJobDetail("eodSettlementJob", "EOD_SETTLEMENT");
    }

    @Bean
    public Trigger eodSettlementTrigger(
            JobDetail eodSettlementJobDetail,
            @Value("${derivops.quartz.eod-settlement-cron:0 5/10 * * * ?}") String cron
    ) {
        return buildTrigger("eodSettlementTrigger", eodSettlementJobDetail, cron);
    }

    private JobDetail buildJobDetail(String jobName, String batchName) {
        return JobBuilder.newJob(QuartzBatchLauncherJob.class)
                .withIdentity(jobName, GROUP)
                .storeDurably()
                .usingJobData("batchName", batchName)
                .build();
    }

    private Trigger buildTrigger(String triggerName, JobDetail jobDetail, String cron) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(triggerName, GROUP)
                .withSchedule(CronScheduleBuilder.cronSchedule(cron))
                .build();
    }
}
