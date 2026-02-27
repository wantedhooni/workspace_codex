package com.portal.admin.repo;

import com.portal.admin.domain.BatchSchedule;
import com.portal.admin.repo.search.FieldSearchableRepository;
import com.portal.admin.repo.search.SearchableRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BatchScheduleRepository extends JpaRepository<BatchSchedule, Long>, SearchableRepository<BatchSchedule>, FieldSearchableRepository<BatchSchedule> {
    Optional<BatchSchedule> findFirstByBatchJobIdAndCronExpression(Long batchJobId, String cronExpression);
}
