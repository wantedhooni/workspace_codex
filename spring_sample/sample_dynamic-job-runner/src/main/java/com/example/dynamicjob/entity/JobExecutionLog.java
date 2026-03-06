package com.example.dynamicjob.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_execution_log")
public class JobExecutionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "job_definition_id", nullable = false)
    private Long jobDefinitionId;

    @Column(name = "job_name", nullable = false, length = 100)
    private String jobName;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Lob
    @Column(name = "error_message")
    private String errorMessage;

    public static JobExecutionLog started(JobDefinition def) {
        JobExecutionLog log = new JobExecutionLog();
        log.jobDefinitionId = def.getId();
        log.jobName = def.getJobName();
        log.status = "RUNNING";
        log.startedAt = LocalDateTime.now();
        return log;
    }

    public void markSuccess() {
        this.status = "SUCCESS";
        this.endedAt = LocalDateTime.now();
    }

    public void markFailed(String errorMessage) {
        this.status = "FAILED";
        this.endedAt = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getJobDefinitionId() { return jobDefinitionId; }
    public void setJobDefinitionId(Long jobDefinitionId) { this.jobDefinitionId = jobDefinitionId; }
    public String getJobName() { return jobName; }
    public void setJobName(String jobName) { this.jobName = jobName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }
    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
