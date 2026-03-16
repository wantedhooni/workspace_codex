package com.example.marketsignal.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.marketsignal.common.JpaConfig;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@DataJpaTest
@Import(JpaConfig.class)
class UserRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private UserRepository userRepository;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    @DisplayName("이메일로 사용자를 조회할 수 있다")
    void findByEmailReturnsSavedUser() {
        userRepository.save(User.builder()
                .email("investor@example.com")
                .password("encoded-password")
                .name("Investor")
                .bio("bio")
                .role(UserRole.ROLE_USER)
                .build());

        Optional<User> result = userRepository.findByEmail("investor@example.com");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Investor");
    }
}
