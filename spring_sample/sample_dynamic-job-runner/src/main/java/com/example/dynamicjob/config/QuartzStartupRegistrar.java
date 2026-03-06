package com.example.dynamicjob.config;

import com.example.dynamicjob.repository.JobDefinitionRepository;
import com.example.dynamicjob.service.JobSchedulingService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class QuartzStartupRegistrar {

    private final JobDefinitionRepository repository;
    private final JobSchedulingService schedulingService;

    public QuartzStartupRegistrar(JobDefinitionRepository repository,
                                  JobSchedulingService schedulingService) {
        this.repository = repository;
        this.schedulingService = schedulingService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void register() {
        schedulingService.scheduleAllEnabled(repository.findAllByEnabledTrue());
    }
}
