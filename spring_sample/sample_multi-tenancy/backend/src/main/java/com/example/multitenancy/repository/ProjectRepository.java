package com.example.multitenancy.repository;

import com.example.multitenancy.domain.Project;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 프로젝트 집계를 위한 JPA 저장소다.
 */
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findAllByTenantIdOrderByCreatedAtDesc(String tenantId);

    Optional<Project> findByIdAndTenantId(Long id, String tenantId);
}
