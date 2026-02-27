package com.portal.admin.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "batch_schedules")
public class BatchSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_job_id", nullable = false)
    private Long batchJobId;

    @Column(name = "cron_expression", nullable = false)
    private String cronExpression;

    @Column(nullable = false)
    private String timezone;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(name = "last_status")
    private String lastStatus;

    @Column(name = "last_run_at")
    private String lastRunAt;

    protected BatchSchedule() {}

    public BatchSchedule(Long batchJobId, String cronExpression, String timezone, Boolean enabled, String lastStatus, String lastRunAt) {
        this.batchJobId = batchJobId;
        this.cronExpression = cronExpression;
        this.timezone = timezone;
        this.enabled = enabled;
        this.lastStatus = lastStatus;
        this.lastRunAt = lastRunAt;
    }

    public Long getId() { return id; }
    public Long getBatchJobId() { return batchJobId; }
    public String getCronExpression() { return cronExpression; }
    public String getTimezone() { return timezone; }
    public Boolean getEnabled() { return enabled; }
    public String getLastStatus() { return lastStatus; }
    public String getLastRunAt() { return lastRunAt; }

    public void setBatchJobId(Long batchJobId) { this.batchJobId = batchJobId; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }
    public void setTimezone(String timezone) { this.timezone = timezone; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public void setLastStatus(String lastStatus) { this.lastStatus = lastStatus; }
    public void setLastRunAt(String lastRunAt) { this.lastRunAt = lastRunAt; }
}
