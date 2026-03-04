package com.revy.scaffolding.user.dto;

import com.revy.scaffolding.user.domain.User;
import com.revy.scaffolding.user.domain.UserStatus;
import java.time.LocalDateTime;

public record UserDetailResponse(
    Long id,
    String email,
    String name,
    UserStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static UserDetailResponse from(User user) {
        return new UserDetailResponse(
            user.getId(),
            user.getEmail(),
            user.getName(),
            user.getStatus(),
            user.getCreatedAt(),
            user.getUpdatedAt()
        );
    }
}

