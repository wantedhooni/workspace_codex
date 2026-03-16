package com.example.marketsignal.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 사용자 프로필 조회 및 수정 API를 제공한다.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserProfileResponse getMe(CurrentUser currentUser) {
        return userService.getMyProfile(currentUser);
    }

    @PatchMapping("/me")
    public UserProfileResponse updateMe(
            CurrentUser currentUser,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return userService.updateMyProfile(currentUser, request);
    }
}
