package com.revy.scaffolding.user.domain.repository;

import com.revy.scaffolding.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long>, UserQueryRepository {
    Optional<User> findByEmail(String email);
}

