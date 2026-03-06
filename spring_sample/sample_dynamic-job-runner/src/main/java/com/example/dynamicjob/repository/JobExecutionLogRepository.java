package com.example.dynamicjob.repository;

import com.example.dynamicjob.entity.JobExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobExecutionLogRepository extends JpaRepository<JobExecutionLog, Long> {
    List<JobExecutionLog> findTop50ByOrderByStartedAtDesc();
}
