package com.tradeauto.config;

import com.tradeauto.service.DailyQuartzJob;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

@Configuration
public class QuartzConfig {
    @Value("${app.scheduling.dailyCron}")
    private String dailyCron;

    @Value("${app.scheduling.zone:America/New_York}")
    private String zone;

    @Bean
    public JobDetail dailyJobDetail() {
        return JobBuilder.newJob(DailyQuartzJob.class)
                .withIdentity("dailyJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger dailyTrigger(JobDetail dailyJobDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(dailyJobDetail)
                .withIdentity("dailyTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule(dailyCron)
                        .inTimeZone(TimeZone.getTimeZone(zone)))
                .build();
    }
}
