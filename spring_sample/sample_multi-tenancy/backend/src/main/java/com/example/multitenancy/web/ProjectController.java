package com.example.multitenancy.web;

import com.example.multitenancy.service.ProjectService;
import com.example.multitenancy.web.dto.CreateProjectRequest;
import com.example.multitenancy.web.dto.DashboardResponse;
import com.example.multitenancy.web.dto.UpdateProjectStatusRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 데모 대시보드에서 사용하는 테넌트 전용 프로젝트 API를 제공한다.
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(projectService.getDashboard());
    }

    @PostMapping
    public ResponseEntity<DashboardResponse.ProjectItem> createProject(@Valid @RequestBody CreateProjectRequest request) {
        return ResponseEntity.ok(projectService.createProject(request));
    }

    @PatchMapping("/{projectId}/status")
    public ResponseEntity<DashboardResponse.ProjectItem> updateProjectStatus(
            @PathVariable Long projectId,
            @Valid @RequestBody UpdateProjectStatusRequest request
    ) {
        return ResponseEntity.ok(projectService.updateProjectStatus(projectId, request));
    }
}
