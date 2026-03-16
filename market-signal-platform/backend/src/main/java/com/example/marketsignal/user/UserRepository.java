package com.example.marketsignal.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 사용자 저장소 접근을 담당한다.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}
