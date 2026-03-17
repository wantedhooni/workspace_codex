package com.example.multitenancy.web.dto;

import com.example.multitenancy.domain.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 새 프로젝트 등록 요청을 표현한다.
 */
public record CreateProjectRequest(
        @NotBlank(message = "프로젝트명은 필수입니다.")
        @Size(max = 120, message = "프로젝트명은 120자를 넘길 수 없습니다.")
        String name,
        @NotBlank(message = "설명은 필수입니다.")
        @Size(max = 500, message = "설명은 500자를 넘길 수 없습니다.")
        String description,
        @NotBlank(message = "오너명은 필수입니다.")
        @Size(max = 120, message = "오너명은 120자를 넘길 수 없습니다.")
        String ownerName,
        @NotNull(message = "상태는 필수입니다.")
        ProjectStatus status
) {
}
