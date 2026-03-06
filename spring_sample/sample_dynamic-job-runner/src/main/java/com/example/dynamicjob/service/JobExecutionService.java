package com.example.dynamicjob.service;

import com.example.dynamicjob.entity.JobDefinition;
import com.example.dynamicjob.entity.JobExecutionLog;
import com.example.dynamicjob.repository.JobExecutionLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JobExecutionService {

    private final SpringBeanMethodInvoker invoker;
    private final JobExecutionLogRepository logRepository;

    public JobExecutionService(SpringBeanMethodInvoker invoker,
                               JobExecutionLogRepository logRepository) {
        this.invoker = invoker;
        this.logRepository = logRepository;
    }

    @Transactional
    public void execute(JobDefinition def) throws Exception {
        JobExecutionLog log = JobExecutionLog.started(def);
        log = logRepository.save(log);
        try {
            invoker.invoke(def);
            log.markSuccess();
            logRepository.save(log);
        } catch (Exception e) {
            log.markFailed(e.getMessage());
            logRepository.save(log);
            throw e;
        }
    }
}
