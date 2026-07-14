package com.example.commerce.user.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.commerce.user.domain.DuplicateEmailException;
import com.example.commerce.user.domain.User;
import com.example.commerce.user.domain.UserNotFoundException;
import com.example.commerce.user.infrastructure.InMemoryUserRepository;

/**
 * 회원 애플리케이션 서비스의 핵심 유스케이스를 검증한다.
 */
class UserServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    private UserService userService;

    /**
     * 각 테스트에 독립적인 메모리 저장소와 고정 시계를 준비한다.
     */
    @BeforeEach
    void setUp() {
        userService = new UserService(
                new InMemoryUserRepository(),
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    /**
     * 회원 등록 시 이메일과 이름을 정규화하고 생성 시각을 기록하는지 검증한다.
     */
    @Test
    void createsUser() {
        User user = userService.createUser(" Buyer@Example.COM ", " 구매자 ");

        assertThat(user.email()).isEqualTo("buyer@example.com");
        assertThat(user.name()).isEqualTo("구매자");
        assertThat(user.createdAt()).isEqualTo(NOW);
        assertThat(userService.getUser(user.id())).isEqualTo(user);
    }

    /**
     * 대소문자만 다른 동일 이메일의 중복 가입을 거부하는지 검증한다.
     */
    @Test
    void rejectsDuplicateEmailIgnoringCase() {
        userService.createUser("buyer@example.com", "첫 회원");

        assertThatThrownBy(() -> userService.createUser("BUYER@example.com", "두 번째 회원"))
                .isInstanceOf(DuplicateEmailException.class);
    }

    /**
     * 존재하지 않는 식별자를 조회하면 명시적인 예외가 발생하는지 검증한다.
     */
    @Test
    void throwsWhenUserDoesNotExist() {
        assertThatThrownBy(() -> userService.getUser(java.util.UUID.randomUUID()))
                .isInstanceOf(UserNotFoundException.class);
    }
}

