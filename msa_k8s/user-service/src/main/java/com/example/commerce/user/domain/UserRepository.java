package com.example.commerce.user.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 회원 저장소 구현과 애플리케이션 서비스 사이의 계약을 정의한다.
 */
public interface UserRepository {

    /**
     * 새 회원을 저장하며 이메일 중복 시 저장을 거부한다.
     *
     * @param user 저장할 회원
     * @return 저장된 회원
     */
    User save(User user);

    /**
     * 식별자로 회원을 조회한다.
     *
     * @param id 회원 식별자
     * @return 존재할 경우 회원
     */
    Optional<User> findById(UUID id);

    /**
     * 이메일로 회원을 조회한다.
     *
     * @param email 정규화된 이메일
     * @return 존재할 경우 회원
     */
    Optional<User> findByEmail(String email);

    /**
     * 가입 시각 순으로 전체 회원을 조회한다.
     *
     * @return 회원 목록
     */
    List<User> findAll();
}

