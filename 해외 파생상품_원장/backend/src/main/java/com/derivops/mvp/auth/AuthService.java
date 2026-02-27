package com.derivops.mvp.auth;

import com.derivops.mvp.audit.AuditLogService;
import com.derivops.mvp.common.BadRequestException;
import com.derivops.mvp.user.UserAccount;
import com.derivops.mvp.user.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class AuthService {

    private static final org.slf4j.Logger SECURITY_AUDIT = org.slf4j.LoggerFactory.getLogger("SECURITY_AUDIT");

    private final AuthenticationManager authenticationManager;
    private final UserAccountRepository userAccountRepository;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;

    public LoginResponse login(LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            UserAccount user = userAccountRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new BadRequestException("User not found"));

            String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
            auditLogService.log(user.getUsername(), "LOGIN_SUCCESS", "AUTH", user.getUsername(), "Successful login");

            return new LoginResponse(token, jwtService.getExpirationSeconds(), user.getRole().name());
        } catch (BadCredentialsException ex) {
            SECURITY_AUDIT.warn("login failed username={} reason=bad_credentials", request.username());
            throw new BadRequestException("Invalid username or password");
        }
    }
}
