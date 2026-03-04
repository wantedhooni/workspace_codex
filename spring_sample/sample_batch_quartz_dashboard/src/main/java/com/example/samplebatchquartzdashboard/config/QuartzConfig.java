package com.example.samplebatchquartzdashboard.config;

import com.example.samplebatchquartzdashboard.batch.BatchDashboardProperties;
import com.example.samplebatchquartzdashboard.quartz.TaskImportQuartzJob;
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

    public static final String JOB_GROUP = "batch-dashboard";
    public static final String JOB_NAME = "taskImportQuartzJob";
    public static final String TRIGGER_NAME = "taskImportCronTrigger";

    @Bean
    public SchedulerFactoryBeanCustomizer schedulerFactoryBeanCustomizer(ApplicationContext applicationContext) {
        return schedulerFactoryBean -> {
            AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
            jobFactory.setApplicationContext(applicationContext);
            schedulerFactoryBean.setJobFactory(jobFactory);
        };
    }

    @Bean
    public JobDetail taskImportJobDetail() {
        return JobBuilder.newJob(TaskImportQuartzJob.class)
                .withIdentity(jobKey())
                .withDescription("Spring Batch task import job trigger")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger taskImportTrigger(JobDetail taskImportJobDetail, BatchDashboardProperties properties) {
        return TriggerBuilder.newTrigger()
                .forJob(taskImportJobDetail)
                .withIdentity(triggerKey())
                .withSchedule(CronScheduleBuilder.cronSchedule(properties.getCron())
                        .withMisfireHandlingInstructionDoNothing())
                .build();
    }

    public static JobKey jobKey() {
        return JobKey.jobKey(JOB_NAME, JOB_GROUP);
    }

    public static TriggerKey triggerKey() {
        return TriggerKey.triggerKey(TRIGGER_NAME, JOB_GROUP);
    }
}
