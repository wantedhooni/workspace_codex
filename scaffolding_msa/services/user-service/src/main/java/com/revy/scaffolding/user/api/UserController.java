package com.revy.scaffolding.user.api;

import com.revy.scaffolding.core.api.ApiResponse;
import com.revy.scaffolding.user.dto.UserCreateRequest;
import com.revy.scaffolding.user.dto.UserDetailResponse;
import com.revy.scaffolding.user.dto.UserSummaryResponse;
import com.revy.scaffolding.user.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ApiResponse<UserDetailResponse> create(@Valid @RequestBody UserCreateRequest request) {
        return ApiResponse.ok(userService.create(request));
    }

    @GetMapping("/{userId}")
    public ApiResponse<UserDetailResponse> get(@PathVariable Long userId) {
        return ApiResponse.ok(userService.get(userId));
    }

    @GetMapping
    public ApiResponse<List<UserSummaryResponse>> search(@RequestParam(required = false) String keyword) {
        return ApiResponse.ok(userService.search(keyword));
    }
}
