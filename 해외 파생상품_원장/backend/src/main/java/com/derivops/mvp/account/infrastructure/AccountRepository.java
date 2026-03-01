package com.derivops.mvp.account.infrastructure;
import com.derivops.mvp.account.*;
import com.derivops.mvp.account.api.*;
import com.derivops.mvp.account.application.*;
import com.derivops.mvp.account.dto.*;


import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long>, AccountRepositoryCustom {
    Optional<Account> findByAccountNo(String accountNo);
}
