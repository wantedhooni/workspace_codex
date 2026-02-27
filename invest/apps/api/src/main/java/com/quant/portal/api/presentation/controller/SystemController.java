package com.quant.portal.api.presentation.controller;

import com.quant.portal.api.application.mapper.CurrentUserResponseMapper;
import com.quant.portal.api.presentation.dto.ApiSingleResponse;
import com.quant.portal.api.presentation.dto.CurrentUserResponse;
import com.quant.portal.api.presentation.dto.PingResponse;
import java.time.Instant;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class SystemController {

    @GetMapping("/public/ping")
    public ResponseEntity<ApiSingleResponse<PingResponse>> publicPing() {
        PingResponse response = new PingResponse("pong", Instant.now());
        return ResponseEntity.ok(new ApiSingleResponse<>(response));
    }

    @GetMapping("/users/me")
    public ResponseEntity<ApiSingleResponse<CurrentUserResponse>> me(Authentication authentication) {
        CurrentUserResponse response = CurrentUserResponseMapper.toDto(authentication);
        return ResponseEntity.ok(new ApiSingleResponse<>(response));
    }

    @GetMapping("/admin/ping")
    public ResponseEntity<ApiSingleResponse<PingResponse>> adminPing() {
        PingResponse response = new PingResponse("admin-pong", Instant.now());
        return ResponseEntity.ok(new ApiSingleResponse<>(response));
    }
}
