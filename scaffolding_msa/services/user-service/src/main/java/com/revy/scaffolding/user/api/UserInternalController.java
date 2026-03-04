package com.revy.scaffolding.user.api;

import com.revy.scaffolding.user.dto.UserLookupResponse;
import com.revy.scaffolding.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
public class UserInternalController {
    private final UserService userService;

    public UserInternalController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public UserLookupResponse getInternal(@PathVariable Long userId) {
        return userService.getLookup(userId);
    }
}

