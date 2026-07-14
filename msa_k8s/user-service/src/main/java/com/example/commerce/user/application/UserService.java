package com.example.commerce.user.application;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import com.example.commerce.user.domain.DuplicateEmailException;
import com.example.commerce.user.domain.User;
import com.example.commerce.user.domain.UserNotFoundException;
import com.example.commerce.user.domain.UserRepository;
import com.example.commerce.user.domain.UserStatus;
import com.example.commerce.metrics.BusinessMetric;

/**
 * 회원 등록과 조회 유스케이스를 조정하는 애플리케이션 서비스다.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final Clock clock;

    /**
     * 회원 저장소와 시계를 주입받아 서비스를 생성한다.
     *
     * @param userRepository 회원 저장소
     * @param clock 가입 시각 생성용 시계
     */
    public UserService(UserRepository userRepository, Clock clock) {
        this.userRepository = userRepository;
        this.clock = clock;
    }

    /**
     * 이메일을 정규화하고 중복을 검사한 뒤 활성 상태의 회원을 등록한다.
     *
     * @param email 회원 이메일
     * @param name 회원 이름
     * @return 생성된 회원
     */
    @Transactional
    @CachePut(cacheNames = "users", key = "#result.id")
    @BusinessMetric("user.create")
    public User createUser(String email, String name) {
        String normalizedEmail = email.strip().toLowerCase(Locale.ROOT);
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new DuplicateEmailException(normalizedEmail);
        }
        User user = new User(
                UUID.randomUUID(),
                normalizedEmail,
                name.strip(),
                UserStatus.ACTIVE,
                Instant.now(clock));
        return userRepository.save(user);
    }

    /**
     * 식별자로 회원을 조회하고 존재하지 않으면 도메인 예외를 발생시킨다.
     *
     * @param id 회원 식별자
     * @return 조회된 회원
     */
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = "users", key = "#id")
    @BusinessMetric("user.get")
    public User getUser(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    /**
     * 가입 순서로 전체 회원을 조회한다.
     *
     * @return 회원 목록
     */
    @Transactional(readOnly = true)
    @BusinessMetric("user.list")
    public List<User> getUsers() {
        return userRepository.findAll();
    }
}
