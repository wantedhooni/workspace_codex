package com.example.samplebatch.config;

import com.example.samplebatch.batch.TradeSettlementProperties;
import com.example.samplebatch.quartz.TradeSettlementQuartzJob;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    public static final String JOB_GROUP = "trade-settlement";
    public static final String JOB_NAME = "tradeSettlementQuartzJob";
    public static final String TRIGGER_NAME = "tradeSettlementCronTrigger";

    @Bean
    public SchedulerFactoryBeanCustomizer schedulerFactoryBeanCustomizer(ApplicationContext applicationContext) {
        return schedulerFactoryBean -> {
            AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
            jobFactory.setApplicationContext(applicationContext);
            schedulerFactoryBean.setJobFactory(jobFactory);
        };
    }

    @Bean
    public JobDetail tradeSettlementJobDetail() {
        return JobBuilder.newJob(TradeSettlementQuartzJob.class)
                .withIdentity(jobKey())
                .withDescription("Spring Batch 거래 정산 잡을 Quartz에서 실행한다.")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger tradeSettlementTrigger(JobDetail tradeSettlementJobDetail, TradeSettlementProperties properties) {
        return TriggerBuilder.newTrigger()
                .forJob(tradeSettlementJobDetail)
                .withIdentity(triggerKey())
                .withSchedule(
                        CronScheduleBuilder.cronSchedule(properties.getCron())
                                .withMisfireHandlingInstructionDoNothing()
                )
                .build();
    }

    public static JobKey jobKey() {
        return JobKey.jobKey(JOB_NAME, JOB_GROUP);
    }

    public static TriggerKey triggerKey() {
        return TriggerKey.triggerKey(TRIGGER_NAME, JOB_GROUP);
    }
}
