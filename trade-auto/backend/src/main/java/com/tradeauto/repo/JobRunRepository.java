package com.tradeauto.repo;

import com.tradeauto.model.JobRun;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface JobRunRepository extends JpaRepository<JobRun, Long> {
    List<JobRun> findTop50ByJobNameOrderByStartedAtDesc(String jobName);
}
