package com.example.multitenancy.web.dto;

import com.example.multitenancy.domain.ProjectStatus;
import jakarta.validation.constraints.NotNull;

/**
 * 프로젝트 상태 변경 요청을 표현한다.
 */
public record UpdateProjectStatusRequest(
        @NotNull(message = "상태는 필수입니다.")
        ProjectStatus status
) {
}
