package com.example.websample.domain.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** 사용자 영속성 처리를 담당하는 저장소입니다. */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
