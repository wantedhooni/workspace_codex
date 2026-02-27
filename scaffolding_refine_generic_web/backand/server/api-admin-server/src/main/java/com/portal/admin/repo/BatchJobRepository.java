package com.portal.admin.repo;

import com.portal.admin.domain.BatchJob;
import com.portal.admin.repo.search.FieldSearchableRepository;
import com.portal.admin.repo.search.SearchableRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BatchJobRepository extends JpaRepository<BatchJob, Long>, SearchableRepository<BatchJob>, FieldSearchableRepository<BatchJob> {
    Optional<BatchJob> findByJobKey(String jobKey);
}
