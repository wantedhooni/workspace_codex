package com.derivops.mvp.account;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AccountRepository extends JpaRepository<Account, Long>, AccountRepositoryCustom, JpaSpecificationExecutor<Account> {
    Optional<Account> findByAccountNo(String accountNo);
}
