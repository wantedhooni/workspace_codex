package com.example.samplebatchquartzdashboard.dashboard;

import com.example.samplebatchquartzdashboard.config.QuartzConfig;
import java.time.Instant;
import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.stereotype.Service;

@Service
public class QuartzControlService {

    private final Scheduler scheduler;

    public QuartzControlService(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    public QuartzActionResponse triggerNow() {
        try {
            scheduler.triggerJob(QuartzConfig.jobKey());
            return toActionResponse("TRIGGERED");
        } catch (SchedulerException exception) {
            throw new IllegalStateException("Quartz 즉시 트리거에 실패했습니다.", exception);
        }
    }

    public QuartzActionResponse pauseTrigger() {
        try {
            scheduler.pauseTrigger(QuartzConfig.triggerKey());
            return toActionResponse("PAUSED");
        } catch (SchedulerException exception) {
            throw new IllegalStateException("Quartz trigger pause 실패", exception);
        }
    }

    public QuartzActionResponse resumeTrigger() {
        try {
            scheduler.resumeTrigger(QuartzConfig.triggerKey());
            return toActionResponse("RESUMED");
        } catch (SchedulerException exception) {
            throw new IllegalStateException("Quartz trigger resume 실패", exception);
        }
    }

    public QuartzActionResponse standbyScheduler() {
        try {
            scheduler.standby();
            return toActionResponse("STANDBY");
        } catch (SchedulerException exception) {
            throw new IllegalStateException("Quartz standby 전환 실패", exception);
        }
    }

    public QuartzActionResponse startScheduler() {
        try {
            scheduler.start();
            return toActionResponse("STARTED");
        } catch (SchedulerException exception) {
            throw new IllegalStateException("Quartz start 실패", exception);
        }
    }

    public QuartzActionResponse updateCron(String cronExpression) {
        if (!CronExpression.isValidExpression(cronExpression)) {
            throw new IllegalArgumentException("유효하지 않은 Cron 표현식입니다: " + cronExpression);
        }
        try {
            Trigger trigger = scheduler.getTrigger(QuartzConfig.triggerKey());
            if (trigger == null) {
                throw new IllegalStateException("Quartz trigger가 존재하지 않습니다.");
            }
            Trigger newTrigger = TriggerBuilder.newTrigger()
                    .forJob(QuartzConfig.jobKey())
                    .withIdentity(QuartzConfig.triggerKey())
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)
                            .withMisfireHandlingInstructionDoNothing())
                    .build();
            scheduler.rescheduleJob(QuartzConfig.triggerKey(), newTrigger);
            return toActionResponse("CRON_UPDATED");
        } catch (SchedulerException exception) {
            throw new IllegalStateException("Quartz cron 변경 실패", exception);
        }
    }

    public QuartzStatusResponse status() {
        try {
            Trigger.TriggerState triggerState = scheduler.getTriggerState(QuartzConfig.triggerKey());
            Trigger trigger = scheduler.getTrigger(QuartzConfig.triggerKey());
            CronTrigger cronTrigger = trigger instanceof CronTrigger ct ? ct : null;
            Instant previousFireTime = trigger != null && trigger.getPreviousFireTime() != null
                    ? trigger.getPreviousFireTime().toInstant()
                    : null;
            Instant nextFireTime = trigger != null && trigger.getNextFireTime() != null
                    ? trigger.getNextFireTime().toInstant()
                    : null;

            boolean started = scheduler.isStarted();
            boolean standbyMode = scheduler.isInStandbyMode();
            boolean shutdown = scheduler.isShutdown();

            return new QuartzStatusResponse(
                    started,
                    standbyMode,
                    shutdown,
                    schedulerState(started, standbyMode, shutdown),
                    triggerState.name(),
                    cronTrigger == null ? "N/A" : cronTrigger.getCronExpression(),
                    previousFireTime,
                    nextFireTime
            );
        } catch (SchedulerException exception) {
            throw new IllegalStateException("Quartz 상태 조회 실패", exception);
        }
    }

    private QuartzActionResponse toActionResponse(String action) throws SchedulerException {
        Trigger.TriggerState triggerState = scheduler.getTriggerState(QuartzConfig.triggerKey());
        Trigger trigger = scheduler.getTrigger(QuartzConfig.triggerKey());
        Instant nextFireTime = trigger != null && trigger.getNextFireTime() != null
                ? trigger.getNextFireTime().toInstant()
                : null;

        boolean started = scheduler.isStarted();
        boolean standbyMode = scheduler.isInStandbyMode();
        boolean shutdown = scheduler.isShutdown();

        return new QuartzActionResponse(
                action,
                schedulerState(started, standbyMode, shutdown),
                triggerState.name(),
                nextFireTime
        );
    }

    private String schedulerState(boolean started, boolean standbyMode, boolean shutdown) {
        if (shutdown) {
            return "SHUTDOWN";
        }
        if (standbyMode) {
            return "STANDBY";
        }
        if (started) {
            return "RUNNING";
        }
        return "INITIALIZED";
    }
}
