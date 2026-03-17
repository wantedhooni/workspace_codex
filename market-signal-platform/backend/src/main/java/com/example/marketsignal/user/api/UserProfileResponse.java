package com.example.marketsignal.user;

import java.time.LocalDateTime;

/**
 * 사용자 프로필 응답 모델이다.
 */
public record UserProfileResponse(
        Long id,
        String email,
        String name,
        String bio,
        LocalDateTime createdAt
) {

    public static UserProfileResponse from(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getBio(),
                user.getCreatedAt()
        );
    }
}
