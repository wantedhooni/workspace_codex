package com.revy.scaffolding.order.client;

import com.revy.scaffolding.order.dto.UserLookupResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", path = "/internal/users")
public interface UserServiceClient {
    @GetMapping("/{userId}")
    UserLookupResponse getUser(@PathVariable("userId") Long userId);
}

