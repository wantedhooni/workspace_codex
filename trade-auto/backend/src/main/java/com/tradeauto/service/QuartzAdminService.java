package com.tradeauto.service;

import com.tradeauto.dto.QuartzJobDTO;
import com.tradeauto.dto.QuartzTriggerDTO;
import com.tradeauto.dto.QuartzTriggerCreateRequest;
import com.tradeauto.dto.QuartzTriggerUpdateRequest;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

@Service
public class QuartzAdminService {
    private final Scheduler scheduler;
    private final QuartzAuditService auditService;

    public QuartzAdminService(Scheduler scheduler, QuartzAuditService auditService) {
        this.scheduler = scheduler;
        this.auditService = auditService;
    }

    public List<QuartzJobDTO> listJobs() throws SchedulerException {
        List<QuartzJobDTO> result = new ArrayList<>();
        for (String group : scheduler.getJobGroupNames()) {
            for (JobKey key : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(group))) {
                JobDetail detail = scheduler.getJobDetail(key);
                QuartzJobDTO dto = new QuartzJobDTO();
                dto.name = key.getName();
                dto.group = key.getGroup();
                dto.description = detail.getDescription();
                dto.durable = detail.isDurable();
                result.add(dto);
            }
        }
        return result;
    }

    public List<QuartzTriggerDTO> listTriggers() throws SchedulerException {
        List<QuartzTriggerDTO> result = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        for (String group : scheduler.getTriggerGroupNames()) {
            for (TriggerKey key : scheduler.getTriggerKeys(GroupMatcher.triggerGroupEquals(group))) {
                Trigger trigger = scheduler.getTrigger(key);
                Trigger.TriggerState state = scheduler.getTriggerState(key);
                QuartzTriggerDTO dto = new QuartzTriggerDTO();
                dto.name = key.getName();
                dto.group = key.getGroup();
                dto.jobName = trigger.getJobKey().getName();
                dto.jobGroup = trigger.getJobKey().getGroup();
                dto.type = trigger instanceof CronTrigger ? "CRON" : trigger.getClass().getSimpleName();
                if (trigger instanceof CronTrigger cron) {
                    dto.cron = cron.getCronExpression();
                    dto.timeZone = cron.getTimeZone().getID();
                }
                dto.state = state.name();
                dto.nextFireTime = formatDate(trigger.getNextFireTime(), fmt);
                dto.prevFireTime = formatDate(trigger.getPreviousFireTime(), fmt);
                result.add(dto);
            }
        }
        return result;
    }

    public QuartzTriggerDTO updateCronTrigger(String triggerName, String triggerGroup, QuartzTriggerUpdateRequest request) throws SchedulerException {
        TriggerKey key = TriggerKey.triggerKey(triggerName, triggerGroup);
        Trigger existing = scheduler.getTrigger(key);
        if (!(existing instanceof CronTrigger)) {
            throw new IllegalStateException("Trigger is not CRON type");
        }
        if (request.cron == null || request.cron.isBlank() || !CronExpression.isValidExpression(request.cron)) {
            throw new IllegalStateException("Invalid cron expression");
        }
        TimeZone tz = request.timeZone == null || request.timeZone.isBlank()
                ? TimeZone.getTimeZone(((CronTrigger) existing).getTimeZone().getID())
                : TimeZone.getTimeZone(request.timeZone);
        CronScheduleBuilder schedule = CronScheduleBuilder.cronSchedule(request.cron).inTimeZone(tz);
        Trigger newTrigger = TriggerBuilder.newTrigger()
                .withIdentity(key)
                .forJob(existing.getJobKey())
                .withSchedule(schedule)
                .build();
        scheduler.rescheduleJob(key, newTrigger);
        auditService.log("TRIGGER_UPDATE", triggerGroup + "/" + triggerName, "cron=" + request.cron + ", tz=" + tz.getID());
        return toDto((CronTrigger) scheduler.getTrigger(key));
    }

    public QuartzTriggerDTO createCronTrigger(QuartzTriggerCreateRequest request) throws SchedulerException {
        if (request == null || request.jobName == null || request.jobGroup == null) {
            throw new IllegalStateException("Job name/group required");
        }
        if (request.triggerName == null || request.triggerGroup == null) {
            throw new IllegalStateException("Trigger name/group required");
        }
        if (request.cron == null || request.cron.isBlank() || !CronExpression.isValidExpression(request.cron)) {
            throw new IllegalStateException("Invalid cron expression");
        }
        JobKey jobKey = JobKey.jobKey(request.jobName, request.jobGroup);
        if (!scheduler.checkExists(jobKey)) {
            throw new IllegalStateException("Job not found");
        }
        TimeZone tz = request.timeZone == null || request.timeZone.isBlank()
                ? TimeZone.getTimeZone("UTC")
                : TimeZone.getTimeZone(request.timeZone);
        TriggerKey triggerKey = TriggerKey.triggerKey(request.triggerName, request.triggerGroup);
        if (scheduler.checkExists(triggerKey)) {
            throw new IllegalStateException("Trigger already exists");
        }
        CronScheduleBuilder schedule = CronScheduleBuilder.cronSchedule(request.cron).inTimeZone(tz);
        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .forJob(jobKey)
                .withSchedule(schedule)
                .build();
        scheduler.scheduleJob(trigger);
        auditService.log("TRIGGER_CREATE", request.triggerGroup + "/" + request.triggerName, "job=" + request.jobGroup + "/" + request.jobName);
        return toDto((CronTrigger) scheduler.getTrigger(triggerKey));
    }

    public void deleteTrigger(String triggerName, String triggerGroup) throws SchedulerException {
        scheduler.unscheduleJob(TriggerKey.triggerKey(triggerName, triggerGroup));
        auditService.log("TRIGGER_DELETE", triggerGroup + "/" + triggerName, "");
    }

    public void pauseTrigger(String triggerName, String triggerGroup) throws SchedulerException {
        scheduler.pauseTrigger(TriggerKey.triggerKey(triggerName, triggerGroup));
        auditService.log("TRIGGER_PAUSE", triggerGroup + "/" + triggerName, "");
    }

    public void resumeTrigger(String triggerName, String triggerGroup) throws SchedulerException {
        scheduler.resumeTrigger(TriggerKey.triggerKey(triggerName, triggerGroup));
        auditService.log("TRIGGER_RESUME", triggerGroup + "/" + triggerName, "");
    }

    public void triggerJob(String jobName, String jobGroup) throws SchedulerException {
        scheduler.triggerJob(JobKey.jobKey(jobName, jobGroup));
        auditService.log("JOB_TRIGGER", jobGroup + "/" + jobName, "");
    }

    public QuartzJobDTO createJob(String name, String group, String className, String description, boolean durable) throws SchedulerException {
        if (name == null || group == null || className == null) {
            throw new IllegalStateException("name/group/className required");
        }
        if (!className.startsWith("com.tradeauto.")) {
            throw new IllegalStateException("className must be in com.tradeauto.*");
        }
        if (!durable) {
            throw new IllegalStateException("durable must be true for standalone job");
        }
        try {
            Class<?> clazz = Class.forName(className);
            if (!Job.class.isAssignableFrom(clazz)) {
                throw new IllegalStateException("className must implement Job");
            }
            JobDetail detail = JobBuilder.newJob((Class<? extends Job>) clazz)
                    .withIdentity(name, group)
                    .withDescription(description)
                    .storeDurably(durable)
                    .build();
            scheduler.addJob(detail, false);
            QuartzJobDTO dto = new QuartzJobDTO();
            dto.name = name;
            dto.group = group;
            dto.description = description;
            dto.durable = durable;
            auditService.log("JOB_CREATE", group + "/" + name, "class=" + className);
            return dto;
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("className not found");
        }
    }

    public void deleteJob(String name, String group) throws SchedulerException {
        scheduler.deleteJob(JobKey.jobKey(name, group));
        auditService.log("JOB_DELETE", group + "/" + name, "");
    }

    private QuartzTriggerDTO toDto(CronTrigger cron) throws SchedulerException {
        QuartzTriggerDTO dto = new QuartzTriggerDTO();
        dto.name = cron.getKey().getName();
        dto.group = cron.getKey().getGroup();
        dto.jobName = cron.getJobKey().getName();
        dto.jobGroup = cron.getJobKey().getGroup();
        dto.type = "CRON";
        dto.cron = cron.getCronExpression();
        dto.timeZone = cron.getTimeZone().getID();
        dto.state = scheduler.getTriggerState(cron.getKey()).name();
        DateTimeFormatter fmt = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        dto.nextFireTime = formatDate(cron.getNextFireTime(), fmt);
        dto.prevFireTime = formatDate(cron.getPreviousFireTime(), fmt);
        return dto;
    }

    private String formatDate(Date date, DateTimeFormatter fmt) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atOffset(java.time.ZoneOffset.UTC).format(fmt);
    }
}
