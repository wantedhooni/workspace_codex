package com.portal.admin.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "batch_jobs", uniqueConstraints = @UniqueConstraint(columnNames = {"job_key"}))
public class BatchJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "job_key", nullable = false)
    private String jobKey;

    private String description;

    @Column(nullable = false)
    private Boolean enabled;

    protected BatchJob() {}

    public BatchJob(String name, String jobKey, String description, Boolean enabled) {
        this.name = name;
        this.jobKey = jobKey;
        this.description = description;
        this.enabled = enabled;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getJobKey() { return jobKey; }
    public String getDescription() { return description; }
    public Boolean getEnabled() { return enabled; }

    public void setName(String name) { this.name = name; }
    public void setJobKey(String jobKey) { this.jobKey = jobKey; }
    public void setDescription(String description) { this.description = description; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
