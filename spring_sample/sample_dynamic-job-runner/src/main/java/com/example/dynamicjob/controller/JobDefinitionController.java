package com.example.dynamicjob.controller;

import com.example.dynamicjob.dto.JobDefinitionRequest;
import com.example.dynamicjob.entity.JobDefinition;
import com.example.dynamicjob.entity.JobExecutionLog;
import com.example.dynamicjob.repository.JobExecutionLogRepository;
import com.example.dynamicjob.service.JobDefinitionService;
import com.example.dynamicjob.service.JobSchedulingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobDefinitionController {

    private final JobDefinitionService jobDefinitionService;
    private final JobSchedulingService jobSchedulingService;
    private final JobExecutionLogRepository jobExecutionLogRepository;

    public JobDefinitionController(JobDefinitionService jobDefinitionService,
                                   JobSchedulingService jobSchedulingService,
                                   JobExecutionLogRepository jobExecutionLogRepository) {
        this.jobDefinitionService = jobDefinitionService;
        this.jobSchedulingService = jobSchedulingService;
        this.jobExecutionLogRepository = jobExecutionLogRepository;
    }

    @GetMapping
    public List<JobDefinition> findAll() {
        return jobDefinitionService.findAll();
    }

    @GetMapping("/{id}")
    public JobDefinition findById(@PathVariable Long id) {
        return jobDefinitionService.findById(id);
    }

    @PostMapping
    public JobDefinition create(@RequestBody JobDefinitionRequest request) {
        return jobDefinitionService.create(request);
    }

    @PutMapping("/{id}")
    public JobDefinition update(@PathVariable Long id, @RequestBody JobDefinitionRequest request) {
        return jobDefinitionService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        jobDefinitionService.delete(id);
    }

    @PostMapping("/{id}/run")
    public void runNow(@PathVariable Long id) {
        jobSchedulingService.triggerNow(id);
    }

    @PostMapping("/{id}/enable")
    public JobDefinition enable(@PathVariable Long id) {
        return jobDefinitionService.enable(id);
    }

    @PostMapping("/{id}/disable")
    public JobDefinition disable(@PathVariable Long id) {
        return jobDefinitionService.disable(id);
    }

    @GetMapping("/logs")
    public List<JobExecutionLog> logs() {
        return jobExecutionLogRepository.findTop50ByOrderByStartedAtDesc();
    }
}
