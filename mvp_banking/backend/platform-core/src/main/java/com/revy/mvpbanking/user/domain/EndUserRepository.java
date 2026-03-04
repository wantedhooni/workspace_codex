package com.revy.mvpbanking.user.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EndUserRepository extends JpaRepository<EndUser, UUID> {
    Optional<EndUser> findByEmail(String email);
    boolean existsByEmail(String email);
}
