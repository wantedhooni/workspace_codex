package com.example.websample.domain.admin;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** 관리자 영속성 처리를 담당하는 저장소입니다. */
public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByEmail(String email);

    boolean existsByEmail(String email);
}
