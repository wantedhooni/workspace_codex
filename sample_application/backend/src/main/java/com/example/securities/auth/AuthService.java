package com.example.securities.auth;

import com.example.securities.auth.AuthDtos.LoginRequest;
import com.example.securities.auth.AuthDtos.LoginResponse;
import com.example.securities.common.BusinessException;
import com.example.securities.security.JwtProperties;
import com.example.securities.security.JwtTokenProvider;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private static final String ADMIN = "admin";
    private static final String AUDITOR = "auditor";

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    public AuthService(JwtTokenProvider jwtTokenProvider, JwtProperties jwtProperties) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
    }

    public LoginResponse login(LoginRequest request) {
        List<String> roles = authenticate(request.username(), request.password());
        String token = jwtTokenProvider.createToken(request.username(), roles);
        return new LoginResponse(token, request.username(), roles, jwtProperties.expirationSeconds());
    }

    private List<String> authenticate(String username, String password) {
        if (ADMIN.equals(username) && "admin1234".equals(password)) {
            return List.of("OPERATOR", "VIEWER");
        }
        if (AUDITOR.equals(username) && "audit1234".equals(password)) {
            return List.of("VIEWER");
        }
        throw new BusinessException("INVALID_CREDENTIALS", "아이디 또는 비밀번호가 올바르지 않습니다.");
    }
}
