package com.quant.mvp.access.infrastructure;

import com.quant.mvp.access.domain.AccessUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccessUserRepository extends JpaRepository<AccessUser, Long>, AccessUserQueryRepository {

    Optional<AccessUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
