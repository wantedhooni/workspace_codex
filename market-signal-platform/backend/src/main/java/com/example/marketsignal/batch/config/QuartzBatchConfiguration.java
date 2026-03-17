package com.example.marketsignal.batch;

import com.example.marketsignal.config.AppProperties;
import java.time.ZoneId;
import java.util.TimeZone;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobDataMap;
import org.quartz.TriggerBuilder;
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Quartz JDBC 스케줄과 JobFactory 구성을 정의한다.
 */
@Configuration
public class QuartzBatchConfiguration {

    /**
     * Quartz Job 인스턴스에도 Spring 의존성 주입이 가능하도록 JobFactory를 등록한다.
     */
    @Bean
    public AutowiringSpringBeanJobFactory autowiringSpringBeanJobFactory() {
        return new AutowiringSpringBeanJobFactory();
    }

    /**
     * SchedulerFactoryBean에 커스텀 JobFactory를 연결한다.
     */
    @Bean
    public SchedulerFactoryBeanCustomizer schedulerFactoryBeanCustomizer(
            AutowiringSpringBeanJobFactory jobFactory
    ) {
        return schedulerFactoryBean -> schedulerFactoryBean.setJobFactory(jobFactory);
    }

    /**
     * 일간 리포트 생성용 Quartz JobDetail을 등록한다.
     */
    @Bean
    public JobDetail dailyReportQuartzJobDetail() {
        JobDataMap dataMap = new JobDataMap();
        dataMap.put(ManagedBatchQuartzJob.MANAGED_JOB_NAME_KEY, ManagedBatchJob.DAILY_REPORT_GENERATION.apiName());

        return JobBuilder.newJob(ManagedBatchQuartzJob.class)
                .withIdentity(ManagedBatchJob.DAILY_REPORT_GENERATION.quartzJobKey())
                .withDescription("평일 아침 리포트 생성 배치를 실행합니다.")
                .usingJobData(dataMap)
                .storeDurably()
                .build();
    }

    /**
     * 일간 리포트 생성용 Quartz CronTrigger를 등록한다.
     */
    @Bean
    public CronTrigger dailyReportQuartzTrigger(
            JobDetail dailyReportQuartzJobDetail,
            AppProperties appProperties
    ) {
        ZoneId zoneId = ZoneId.of(appProperties.batch().report().zoneId());

        return TriggerBuilder.newTrigger()
                .forJob(dailyReportQuartzJobDetail)
                .withIdentity(ManagedBatchJob.DAILY_REPORT_GENERATION.quartzTriggerKey())
                .withDescription("평일 장 시작 전 리포트를 생성합니다.")
                .withSchedule(CronScheduleBuilder.cronSchedule(appProperties.batch().report().cron())
                        .inTimeZone(TimeZone.getTimeZone(zoneId))
                        .withMisfireHandlingInstructionDoNothing())
                .build();
    }
}
