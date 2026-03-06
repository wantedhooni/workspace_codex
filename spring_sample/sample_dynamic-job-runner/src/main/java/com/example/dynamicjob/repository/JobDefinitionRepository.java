package com.example.dynamicjob.repository;

import com.example.dynamicjob.entity.JobDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JobDefinitionRepository extends JpaRepository<JobDefinition, Long> {
    List<JobDefinition> findAllByEnabledTrue();
    Optional<JobDefinition> findByJobName(String jobName);
}
