package com.example.websample.domain.user;

import com.example.websample.global.security.jwt.JwtPrincipal;
import com.example.websample.global.security.jwt.JwtPrincipalLoader;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** JWT 토큰의 USER 주체를 User 엔티티 기반 인증 주체로 복원합니다. */
@Component
public class UserJwtPrincipalLoader implements JwtPrincipalLoader {

    private final UserRepository userRepository;

    public UserJwtPrincipalLoader(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public String supports() {
        return UserJwtPrincipal.TYPE;
    }

    @Override
    public Optional<JwtPrincipal> load(Long principalId) {
        return userRepository.findById(principalId).map(UserJwtPrincipal::from);
    }
}
