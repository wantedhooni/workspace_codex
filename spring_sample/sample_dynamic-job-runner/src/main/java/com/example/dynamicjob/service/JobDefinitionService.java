package com.example.dynamicjob.service;

import com.example.dynamicjob.dto.JobDefinitionRequest;
import com.example.dynamicjob.entity.JobDefinition;
import com.example.dynamicjob.repository.JobDefinitionRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class JobDefinitionService {

    private final JobDefinitionRepository repository;
    private final JobSchedulingService schedulingService;
    private final ApplicationContext applicationContext;

    public JobDefinitionService(JobDefinitionRepository repository,
                                JobSchedulingService schedulingService,
                                ApplicationContext applicationContext) {
        this.repository = repository;
        this.schedulingService = schedulingService;
        this.applicationContext = applicationContext;
    }

    @Transactional(readOnly = true)
    public List<JobDefinition> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public JobDefinition findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job definition not found: " + id));
    }

    public JobDefinition create(JobDefinitionRequest request) {
        validateBeanExists(request.getBeanName());
        JobDefinition def = toEntity(new JobDefinition(), request);
        JobDefinition saved = repository.save(def);
        schedulingService.scheduleOrReplace(saved);
        return saved;
    }

    public JobDefinition update(Long id, JobDefinitionRequest request) {
        validateBeanExists(request.getBeanName());
        JobDefinition def = findById(id);
        JobDefinition saved = repository.save(toEntity(def, request));
        schedulingService.scheduleOrReplace(saved);
        return saved;
    }

    public void delete(Long id) {
        JobDefinition def = findById(id);
        schedulingService.unschedule(def.getJobName());
        repository.delete(def);
    }

    public JobDefinition enable(Long id) {
        JobDefinition def = findById(id);
        def.setEnabled(true);
        JobDefinition saved = repository.save(def);
        schedulingService.scheduleOrReplace(saved);
        return saved;
    }

    public JobDefinition disable(Long id) {
        JobDefinition def = findById(id);
        def.setEnabled(false);
        JobDefinition saved = repository.save(def);
        schedulingService.unschedule(saved.getJobName());
        return saved;
    }

    private JobDefinition toEntity(JobDefinition def, JobDefinitionRequest request) {
        def.setJobName(request.getJobName());
        def.setCronExpr(request.getCronExpr());
        def.setBeanName(request.getBeanName());
        def.setMethodName(request.getMethodName());
        def.setArgTypesJson(request.getArgTypesJson());
        def.setArgsJson(request.getArgsJson());
        def.setEnabled(request.isEnabled());
        return def;
    }

    private void validateBeanExists(String beanName) {
        if (!applicationContext.containsBean(beanName)) {
            throw new IllegalArgumentException("Spring bean not found: " + beanName);
        }
    }
}
