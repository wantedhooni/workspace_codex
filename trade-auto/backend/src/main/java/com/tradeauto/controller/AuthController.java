package com.tradeauto.controller;

import com.tradeauto.dto.ApiResponse;
import com.tradeauto.dto.AuthRequest;
import com.tradeauto.dto.AuthResponse;
import com.tradeauto.service.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@CrossOrigin
public class AuthController {
    private final JwtService jwtService;
    private final AuthenticationManager authManager;

    public AuthController(JwtService jwtService, UserDetailsService userDetailsService, PasswordEncoder encoder) {
        this.jwtService = jwtService;
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(encoder);
        this.authManager = provider::authenticate;
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@RequestBody AuthRequest request) {
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(request.username, request.password));
            String token = jwtService.generateToken(request.username);
            return ApiResponse.ok(new AuthResponse(token));
        } catch (AuthenticationException ex) {
            return ApiResponse.error("Invalid credentials");
        }
    }
}
