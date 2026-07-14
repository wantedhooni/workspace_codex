package com.example.commerce.user.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import com.example.commerce.user.domain.DuplicateEmailException;
import com.example.commerce.user.domain.User;
import com.example.commerce.user.domain.UserRepository;

/**
 * PostgreSQL을 회원 원본 저장소로 사용하는 도메인 저장소 구현체다.
 */
@Repository
public class PostgresUserRepository implements UserRepository {

    private final SpringDataUserJpaRepository repository;

    /**
     * Spring Data JPA 저장소를 주입받는다.
     *
     * @param repository 회원 JPA 저장소
     */
    public PostgresUserRepository(SpringDataUserJpaRepository repository) {
        this.repository = repository;
    }

    /**
     * 회원을 저장하고 DB 고유 제약 위반을 도메인 중복 예외로 변환한다.
     *
     * @param user 저장할 회원
     * @return 저장된 회원
     */
    @Override
    public User save(User user) {
        try {
            return repository.saveAndFlush(UserEntity.from(user)).toDomain();
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateEmailException(user.email());
        }
    }

    /**
     * 식별자로 회원을 조회한다.
     *
     * @param id 회원 식별자
     * @return 존재할 경우 회원
     */
    @Override
    public Optional<User> findById(UUID id) {
        return repository.findById(id).map(UserEntity::toDomain);
    }

    /**
     * 정규화된 이메일로 회원을 조회한다.
     *
     * @param email 정규화된 이메일
     * @return 존재할 경우 회원
     */
    @Override
    public Optional<User> findByEmail(String email) {
        return repository.findByEmail(email).map(UserEntity::toDomain);
    }

    /**
     * 가입 시각과 식별자 순서로 전체 회원을 조회한다.
     *
     * @return 정렬된 회원 목록
     */
    @Override
    public List<User> findAll() {
        return repository.findAllByOrderByCreatedAtAscIdAsc().stream()
                .map(UserEntity::toDomain)
                .toList();
    }
}

