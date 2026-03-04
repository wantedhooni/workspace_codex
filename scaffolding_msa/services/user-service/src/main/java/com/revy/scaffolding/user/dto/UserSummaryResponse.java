package com.revy.scaffolding.user.dto;

import com.revy.scaffolding.user.domain.User;
import com.revy.scaffolding.user.domain.UserStatus;

public record UserSummaryResponse(
    Long id,
    String email,
    String name,
    UserStatus status
) {
    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(user.getId(), user.getEmail(), user.getName(), user.getStatus());
    }
}

