package com.example.multitenancy.service;

import com.example.multitenancy.domain.Project;
import com.example.multitenancy.repository.ProjectRepository;
import com.example.multitenancy.security.AuthenticatedUser;
import com.example.multitenancy.web.dto.CreateProjectRequest;
import com.example.multitenancy.web.dto.DashboardResponse;
import com.example.multitenancy.web.dto.UpdateProjectStatusRequest;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 테넌트 범위 안에서 프로젝트 조회와 등록을 처리한다.
 */
@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TenantFilterEnabler tenantFilterEnabler;
    private final CurrentUserService currentUserService;

    public ProjectService(ProjectRepository projectRepository, TenantFilterEnabler tenantFilterEnabler, CurrentUserService currentUserService) {
        this.projectRepository = projectRepository;
        this.tenantFilterEnabler = tenantFilterEnabler;
        this.currentUserService = currentUserService;
    }

    /**
     * 현재 로그인한 테넌트의 대시보드 데이터를 반환한다.
     */
    @Transactional(readOnly = true)
    public DashboardResponse getDashboard() {
        tenantFilterEnabler.enableCurrentTenantFilter();
        AuthenticatedUser currentUser = currentUserService.getCurrentUser();

        List<Project> projects = projectRepository.findAllByTenantIdOrderByCreatedAtDesc(currentUser.tenantId());

        List<DashboardResponse.StatusCount> statusCounts = Arrays.stream(com.example.multitenancy.domain.ProjectStatus.values())
                .map(status -> new DashboardResponse.StatusCount(
                        status,
                        projects.stream().filter(project -> project.getStatus() == status).count()
                ))
                .toList();

        return new DashboardResponse(
                new DashboardResponse.TenantSummary(
                        currentUser.tenantId(),
                        currentUser.displayName(),
                        currentUser.role()
                ),
                projects.stream()
                        .map(project -> new DashboardResponse.ProjectItem(
                                project.getId(),
                                project.getName(),
                                project.getDescription(),
                                project.getOwnerName(),
                                project.getStatus(),
                                project.getCreatedAt(),
                                project.getUpdatedAt()
                        ))
                        .toList(),
                statusCounts
        );
    }

    /**
     * 현재 테넌트에 새 프로젝트를 등록한다.
     */
    @Transactional
    public DashboardResponse.ProjectItem createProject(CreateProjectRequest request) {
        tenantFilterEnabler.enableCurrentTenantFilter();
        AuthenticatedUser currentUser = currentUserService.getCurrentUser();

        Project project = new Project();
        project.setTenantId(currentUser.tenantId());
        project.setName(request.name());
        project.setDescription(request.description());
        project.setOwnerName(request.ownerName());
        project.setStatus(request.status());

        Project savedProject = projectRepository.save(project);
        return new DashboardResponse.ProjectItem(
                savedProject.getId(),
                savedProject.getName(),
                savedProject.getDescription(),
                savedProject.getOwnerName(),
                savedProject.getStatus(),
                savedProject.getCreatedAt(),
                savedProject.getUpdatedAt()
        );
    }

    /**
     * 현재 테넌트가 보유한 프로젝트의 상태를 변경한다.
     */
    @Transactional
    public DashboardResponse.ProjectItem updateProjectStatus(Long projectId, UpdateProjectStatusRequest request) {
        tenantFilterEnabler.enableCurrentTenantFilter();
        AuthenticatedUser currentUser = currentUserService.getCurrentUser();

        Project project = projectRepository.findByIdAndTenantId(projectId, currentUser.tenantId())
                .orElseThrow(() -> new IllegalArgumentException("현재 테넌트에서 접근 가능한 프로젝트가 없습니다."));

        project.setStatus(request.status());
        Project savedProject = projectRepository.save(project);

        return new DashboardResponse.ProjectItem(
                savedProject.getId(),
                savedProject.getName(),
                savedProject.getDescription(),
                savedProject.getOwnerName(),
                savedProject.getStatus(),
                savedProject.getCreatedAt(),
                savedProject.getUpdatedAt()
        );
    }
}
