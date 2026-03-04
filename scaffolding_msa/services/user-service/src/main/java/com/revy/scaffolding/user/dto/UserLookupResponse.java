package com.revy.scaffolding.user.dto;

import com.revy.scaffolding.user.domain.User;

public record UserLookupResponse(
    Long id,
    String email,
    String name
) {
    public static UserLookupResponse from(User user) {
        return new UserLookupResponse(user.getId(), user.getEmail(), user.getName());
    }
}

